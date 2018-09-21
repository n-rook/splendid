package com.nrook.splendid.rules

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.Multiset
import com.google.common.collect.Multisets
import com.nrook.splendid.rules.moves.BuyDevelopment
import com.nrook.splendid.rules.moves.Move
import com.nrook.splendid.rules.moves.ReserveDevelopment
import com.nrook.splendid.rules.moves.TakeTokens
import kotlin.math.max

/**
 * Game rules and state.
 */
data class Game(
    val turn: Turn,
    val developments: Developments,
    val nobles: ImmutableSet<Noble>,
    val chips: ImmutableMultiset<ChipColor>,
    val tableaux: ImmutableMap<Player, Tableau>) {
  fun winner(): Player? {
    // In Splendor, play continues until all players have played the same number of rounds.
    if (turn.player == Player.TWO) {
      return null
    }

    val points = tableaux.mapValues { it.value.victoryPoints }
    val eligibleWinners = points.filter { it.value >= 15 }.keys

    return when(eligibleWinners.size) {
      0 -> null
      1 -> eligibleWinners.first()
      else -> {
        if (points[Player.ONE]!! > points[Player.TWO]!!) {
          return Player.ONE
        } else if (points[Player.TWO]!! > points[Player.ONE]!!) {
          return Player.TWO
        }

        // Tiebreaker: Number of development cards. Lowest wins.
        val devCount = tableaux.mapValues { it.value.developments.size }
        if (devCount[Player.ONE]!! != devCount[Player.TWO]!!) {
          return devCount.minBy { it.value }!!.key
        }

        // In the event of a tie, arbitrarily give it to player two.
        // TODO: Kind of bad! Not in the rules!
        return Player.TWO
      }
    }
  }

  /**
   * Returns a list of all legal moves in this position.
   */
  fun moves(): ImmutableList<Move> {
    // NOTE: There is an issue where moves() is sometimes zero.
    // This is because we have not yet implemented "take some chips and put them back".
    return ImmutableList.builder<Move>()
        .addAll(take3ChipsMoves())
        .addAll(take2IdenticalChipsMoves())
        .addAll(buyDevelopmentCardMoves())
        .addAll(reserveDevelopmentMoves())
        .build()
  }

  /**
   * Returns available chips other than the gold ones.
   */
  fun regularChips(): Multiset<ChipColor> {
    return Multisets.filter(chips) { it: ChipColor? -> it != ChipColor.GOLD }
  }

  private fun take3ChipsMoves(): ImmutableList<TakeTokens> {
    val availableChips = ImmutableMultiset.copyOf(regularChips())
    val playerChips = tableaux[turn.player]!!.chips
    val giveCount: Int = haveToGiveBack(turn.player, 3)

    val options = takeDifferentChips(availableChips, playerChips, 3, giveCount)
    return ImmutableList.copyOf(
        options.map { TakeTokens(it.take, it.give) })
  }

  private fun take2IdenticalChipsMoves(): ImmutableList<TakeTokens> {
    val availableChips = run {
      val builder = ImmutableMultiset.builder<ChipColor>()
      for (entry in regularChips().entrySet()) {
        if (entry.count >= THRESHOLD_TO_TAKE_IDENTICAL_CHIPS) {
          builder.addCopies(entry.element, entry.count)
        }
      }

      builder.build()
    }

    val playerChips = tableaux[turn.player]!!.chips
    val giveCount: Int = haveToGiveBack(turn.player, 2)
    val options = takeSameChips(availableChips, playerChips, 2, giveCount)
    return ImmutableList.copyOf(options.map { TakeTokens(it.take, it.give) })
  }

  // If the given player took some chips, this is how many they would have to give back.
  private fun haveToGiveBack(player: Player, numTaken: Int): Int {
    return max(tableaux[player]!!.chips.size + numTaken - MAX_TOKENS, 0)
  }

  private fun reserveDevelopmentMoves(): ImmutableList<ReserveDevelopment> {
    if (tableaux[turn.player]!!.reservedDevelopments.size >= MAX_RESERVED_DEVELOPMENTS) {
      return ImmutableList.of()
    }

    return ImmutableList.copyOf(developments.cards().map { ReserveDevelopment(it) })
  }

  private fun buyDevelopmentCardMoves(): ImmutableList<BuyDevelopment> {
    val tableau: Tableau = tableaux[turn.player]!!
    val buyOpenCards = ImmutableList.copyOf(
        developments.cards().filter { tableau.canAfford(it) }
            .flatMap {
              val minimalPrice = tableau.minimalPrice(it)

              val possibleNobles = nobles.filter {
                n: Noble -> tableau.receivesVisit(it, n)
              }
              if (possibleNobles.isEmpty()) {
                return@flatMap ImmutableList.of<BuyDevelopment>(
                    BuyDevelopment(it, minimalPrice, null)
                )
              } else {
                return@flatMap possibleNobles.map { n: Noble -> BuyDevelopment(it, minimalPrice, n) }
              }
            }
    )
//            .map { BuyDevelopment(it, tableau.minimalPrice(it)) })

    // TODO: Present opportunity to spend extra gold for no reason
    // TODO: Buy reserved development cards

    return buyOpenCards
  }

  fun takeMove(move: Move): Game {
    val theWinner = winner()
    if (theWinner != null) {
      throw Error("Cannot take move; $theWinner already won")
    }

    return when(move) {
      is TakeTokens -> takeTokensMove(move)
      is BuyDevelopment -> buyDevelopmentMove(move)
      is ReserveDevelopment -> reserveDevelopmentMove(move)
      else -> TODO()
    }
  }

  private fun takeTokensMove(move: TakeTokens): Game {
    val actor = turn.player
    val actingTableau = tableaux[actor]!!

    if (!Multisets.containsOccurrences(chips, move.tokens)) {
      throw Error("Illegal takeTokens move: tokens not present to be taken")
    }
    if (!Multisets.containsOccurrences(actingTableau.chips, move.returnedTokens)) {
      throw Error("Illegal takeTokens move: tokens not present to be returned")
    }

    val newCommonChips = ImmutableMultiset.copyOf(
        Multisets.sum(Multisets.difference(chips, move.tokens), move.returnedTokens))
    val newTableau = actingTableau.toBuilder()
        .addChips(move.tokens)
        .subtractChips(move.returnedTokens)
        .build()
    return Game(
        turn.next(),
        developments,
        nobles,
        newCommonChips,
        ImmutableMap.of(
            actor, newTableau,
            actor.opponent(), tableaux[actor.opponent()]!!))
  }

  /**
   * Give chips from the central store to a given player.
   */
  fun takeChips(player: Player, chipsToTake: ImmutableMultiset<ChipColor>): Game {
    val receivingTableau = tableaux[player]!!

    if (!Multisets.containsOccurrences(chips, chipsToTake)) {
      throw Error("Illegal takeTokens move: tokens not present to be taken")
    }

    val newCommonChips = Multisets.difference(chips, chipsToTake)
    val newTableau = receivingTableau.addChips(chipsToTake)
    return Game(
        turn,
        developments,
        nobles,
        ImmutableMultiset.copyOf(newCommonChips),
        ImmutableMap.of(
            player, newTableau,
            player.opponent(), tableaux[player.opponent()]!!))
  }

  fun reserveDevelopmentMoveGetsGold(): Boolean {
    return chips.count(ChipColor.GOLD) > 0
  }

  private fun reserveDevelopmentMove(move: ReserveDevelopment): Game {
    val actor = turn.player
    val actingTableau = tableaux[actor]!!
    val newTableau = actingTableau.toBuilder()
        .addReservedDevelopment(move.card)
        .addChips(
            if (reserveDevelopmentMoveGetsGold())
              ImmutableMultiset.of(ChipColor.GOLD)
            else ImmutableMultiset.of())
        .build()
    val updatedDevelopments = developments.removeCard(move.card)

    val newCommonChips = if (reserveDevelopmentMoveGetsGold())
      Multisets.difference(chips, ImmutableMultiset.of(ChipColor.GOLD))
    else chips


    // I forgot to give you the gold!
    return Game(
        turn.next(),
        updatedDevelopments,
        nobles,
        ImmutableMultiset.copyOf(newCommonChips),
        ImmutableMap.of(
            actor, newTableau,
            actor.opponent(), tableaux[actor.opponent()]!!
        )
    )
  }

  private fun buyDevelopmentMove(move: BuyDevelopment): Game {
    val actor = turn.player
    val actingTableau = tableaux[actor]!!
    val newTableau = actingTableau.toBuilder()
        .subtractChips(move.price)
        .addDevelopment(move.card)
        .build()
    val updatedChips = ImmutableMultiset.copyOf(Multisets.sum(chips, move.price))

    val newDevelopments = developments.removeCard(move.card)
    return Game(
        turn.next(),
        newDevelopments,
        nobles,
        updatedChips,
        ImmutableMap.of(
            actor, newTableau,
            actor.opponent(), tableaux[actor.opponent()]!!))
  }
}

fun setupNewGame(
    startingDevelopments: ImmutableSetMultimap<Row, DevelopmentCard>,
    remainingDevelopments: ImmutableListMultimap<Row, DevelopmentCard>,
    nobles: ImmutableSet<Noble>): Game {
  val decks: ImmutableMap<Row, Deck> = run {
    val builder = ImmutableMap.builder<Row, Deck>()
    for (row in Row.values()) {
      builder.put(row, Deck(remainingDevelopments[row]))
    }
    return@run builder.build()
  }

  val developments = Developments(run {
    val builder = ImmutableMap.builder<Row, DevelopmentRow>()
    for (row in Row.values()) {
      builder.put(row, DevelopmentRow(startingDevelopments[row]!!, decks[row]!!))
    }
    builder.build()
  })

  return Game(
      Turn.START,
      developments,
      nobles,
      STARTING_CHIPS,
      ImmutableMap.of(Player.ONE, EMPTY_TABLEAU, Player.TWO, EMPTY_TABLEAU)
  )
}

val STARTING_CHIPS: ImmutableMultiset<ChipColor> = run {
  val builder: ImmutableMultiset.Builder<ChipColor> = ImmutableMultiset.builder()
  for (color in ChipColor.values()) {
    val count = if (color == ChipColor.GOLD) 5 else 4
    builder.addCopies(color, count)
  }
  builder.build()
}

