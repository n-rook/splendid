package com.nrook.splendid.rules

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.Multisets
import com.google.common.collect.Sets
import com.nrook.splendid.rules.moves.BuyDevelopment
import com.nrook.splendid.rules.moves.Move
import com.nrook.splendid.rules.moves.TakeTokens

/**
 * Game rules and state.
 */
data class Game(
    val turn: Turn,
    val developments: Developments,
    val nobles: ImmutableSet<Noble>,
    val chips: ImmutableMultiset<ChipColor>,
    val tableaux: ImmutableMap<Player, Tableau>,
    val decks: ImmutableMap<Row, Deck>) {
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
    return ImmutableList.builder<Move>()
        .addAll(take3ChipsMoves())
        .addAll(take2IdenticalChipsMoves())
        .addAll(buyDevelopmentCardMoves())
        .build()
  }

  fun take3ChipsMoves(): ImmutableList<TakeTokens> {
    val availableChips = ImmutableSet.copyOf(
        chips.entrySet().map { it.element }.filter { it != ChipColor.GOLD })
    if (availableChips.isEmpty()) {
      return ImmutableList.of()
    }
    if (availableChips.size <= 3) {
      return ImmutableList.of(TakeTokens(ImmutableMultiset.copyOf(availableChips)))
    }
    return ImmutableList.copyOf(
        Sets.combinations(availableChips, 3)
            .map { TakeTokens(ImmutableMultiset.copyOf(it)) }
    )
  }

  fun take2IdenticalChipsMoves(): ImmutableList<TakeTokens> {
    val availableChips = chips.entrySet().filter { it.count >= 2 }.map { it.element }
    return ImmutableList.copyOf(availableChips.map { TakeTokens(ImmutableMultiset.of(it, it)) })
  }

  fun buyDevelopmentCardMoves(): ImmutableList<BuyDevelopment> {
    val tableau: Tableau = tableaux[turn.player]!!
    val buyOpenCards = ImmutableList.copyOf(
        developments.filter { tableau.canAfford(it) }
            .map { BuyDevelopment(it, tableau.minimalPrice(it)) })

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
      else -> TODO()
    }
  }

  private fun takeTokensMove(move: TakeTokens): Game {
    val actor = turn.player
    val actingTableau = tableaux[actor]!!

    if (!Multisets.containsOccurrences(chips, move.tokens)) {
      throw Error("Illegal takeTokens move")
    }

    val newCommonChips = ImmutableMultiset.copyOf(Multisets.difference(chips, move.tokens))
    val newTableau = actingTableau.addChips(move.tokens)
    return Game(
        turn.next(),
        developments,
        nobles,
        newCommonChips,
        ImmutableMap.of(
            actor, newTableau,
            actor.opponent(), tableaux[actor.opponent()]!!),
        decks)
  }

  private fun buyDevelopmentMove(move: BuyDevelopment): Game {
    val actor = turn.player
    val actingTableau = tableaux[actor]!!
    val newTableau = actingTableau.toBuilder()
        .subtractChips(move.price)
        .addDevelopment(move.card)
        .build()
    val updatedChips = ImmutableMultiset.copyOf(Multisets.sum(chips, move.price))

    // TODO: Rewrite Developments to contain Decks too, and make this a method there
    val row = Row.values().firstOrNull { developments.rows.containsEntry(it, move.card) }
        ?: throw Error("Cannot find development card in common area")

    val (newDeck, newRow) = drawReplacement(move.card, decks[row]!!, developments.rows[row])
    val newDecks = kotlin.run {
      val map = HashMap(decks)
      map[row] = newDeck
      ImmutableMap.copyOf(map)
    }

    val newDevelopments = developments.replaceRow(row, newRow)

    return Game(
        turn.next(),
        newDevelopments,
        nobles,
        updatedChips,
        ImmutableMap.of(
            actor, newTableau,
            actor.opponent(), tableaux[actor.opponent()]!!),
        newDecks)
  }
}

private fun drawReplacement(d: DevelopmentCard, deck: Deck, row: Set<DevelopmentCard>):
    Pair<Deck, Set<DevelopmentCard>> {
  if (deck.isEmpty()) {
    val newRow = ImmutableSet.copyOf(row.minus(d))
    return Pair(deck, newRow)
  }

  val (replacement, newDeck) = deck.draw()
  val newRow = ImmutableSet.copyOf(row.minus(d).plus(replacement))
  return Pair(newDeck, newRow)
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

  return Game(
      Turn(1, Player.ONE),
      Developments(startingDevelopments),
      nobles,
      STARTING_CHIPS,
      ImmutableMap.of(Player.ONE, EMPTY_TABLEAU, Player.TWO, EMPTY_TABLEAU),
      decks
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

