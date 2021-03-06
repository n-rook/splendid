package com.nrook.splendid.rules

import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.ImmutableSet
import com.google.common.truth.Truth
import com.nrook.splendid.rules.cards.testing.createStandardTestGame
import com.nrook.splendid.rules.moves.BuyDevelopment
import com.nrook.splendid.rules.moves.ReserveDevelopment
import com.nrook.splendid.rules.moves.TakeTokens
import com.nrook.splendid.rules.testing.assertChips
import org.junit.Before
import org.junit.Test

class GameTest {

  lateinit var game: Game

  @Before
  fun setUp() {
    game = createStandardTestGame()
  }

  @Test
  fun takeChipsHasExpectedMoves() {
    val chipMoves = ImmutableSet.copyOf(game.moves().filterIsInstance(TakeTokens::class.java))

    Truth.assertThat(chipMoves).contains(
        TakeTokens(ImmutableMultiset.of(ChipColor.RED, ChipColor.BLACK, ChipColor.WHITE),
            ImmutableMultiset.of()))
    Truth.assertThat(chipMoves).contains(
        TakeTokens(ImmutableMultiset.of(ChipColor.RED, ChipColor.RED),
            ImmutableMultiset.of()))
  }

  @Test
  fun takeChipsUtility() {
    val tookChips = game.takeChips(Player.TWO, ImmutableMultiset.of(
        ChipColor.GREEN, ChipColor.GREEN, ChipColor.GREEN, ChipColor.BLUE, ChipColor.GOLD))
    assertChips(tookChips.chips, 4, 4, 3, 1, 4, 4)
    Truth.assertThat(tookChips.tableaux[Player.ONE]!!.chips).isEmpty()
    assertChips(tookChips.tableaux[Player.TWO]!!.chips,
        1, 0, 1, 3, 0, 0)
  }

  @Test
  fun moveTakesChips() {
    val tookChips = game.takeMove(
        TakeTokens(
            ImmutableMultiset.of(ChipColor.RED, ChipColor.BLACK, ChipColor.WHITE),
            ImmutableMultiset.of()))
    Truth.assertThat(tookChips.turn).isEqualTo(Turn(0, Player.TWO))
    Truth.assertThat(tookChips.chips).hasCount(ChipColor.GOLD, 5)
    Truth.assertThat(tookChips.chips).hasCount(ChipColor.RED, 3)
    Truth.assertThat(tookChips.chips).hasCount(ChipColor.BLACK, 3)
    Truth.assertThat(tookChips.chips).hasCount(ChipColor.WHITE, 3)
    Truth.assertThat(tookChips.chips).hasCount(ChipColor.BLUE, 4)
    Truth.assertThat(tookChips.chips).hasCount(ChipColor.GREEN, 4)

    val p1chips = tookChips.tableaux[Player.ONE]!!.chips
    Truth.assertThat(p1chips).hasCount(ChipColor.GOLD, 0)
    Truth.assertThat(p1chips).hasCount(ChipColor.RED, 1)
    Truth.assertThat(p1chips).hasCount(ChipColor.BLACK, 1)
    Truth.assertThat(p1chips).hasCount(ChipColor.WHITE, 1)
    Truth.assertThat(p1chips).hasCount(ChipColor.BLUE, 0)
    Truth.assertThat(p1chips).hasCount(ChipColor.GREEN, 0)

    Truth.assertThat(tookChips.tableaux[Player.TWO]!!.chips).isEmpty()
  }

  @Test
  fun hasExpectedMovesWhenReturningChips() {
    val tookChips = game.takeChips(Player.ONE, ImmutableMultiset.builder<ChipColor>()
        .addCopies(ChipColor.GREEN, 4)
        .addCopies(ChipColor.RED, 3)
        .addCopies(ChipColor.WHITE, 2)
        .build())
    val chipMoves = ImmutableSet.copyOf(tookChips.moves().filterIsInstance(TakeTokens::class.java))

    Truth.assertThat(chipMoves).doesNotContain(TakeTokens(
        ImmutableMultiset.of(ChipColor.GREEN, ChipColor.WHITE, ChipColor.BLACK),
        ImmutableMultiset.of()
    ))
    Truth.assertThat(chipMoves).doesNotContain(TakeTokens(
        ImmutableMultiset.of(ChipColor.GREEN, ChipColor.GREEN),
        ImmutableMultiset.of()
    ))
    Truth.assertThat(chipMoves).doesNotContain(TakeTokens(
        ImmutableMultiset.of(ChipColor.RED, ChipColor.RED),
        ImmutableMultiset.of()
    ))

    Truth.assertThat(chipMoves).contains(TakeTokens(
        ImmutableMultiset.of(ChipColor.WHITE, ChipColor.BLUE, ChipColor.BLACK),
        ImmutableMultiset.of(ChipColor.GREEN, ChipColor.GREEN)
    ))
    // "Take 2" does not support this yet
    Truth.assertThat(chipMoves).contains(TakeTokens(
        ImmutableMultiset.of(ChipColor.BLACK, ChipColor.BLACK),
        ImmutableMultiset.of(ChipColor.RED)
    ))
    Truth.assertThat(chipMoves).contains(TakeTokens(
        // Took and gave back red and white chips
        ImmutableMultiset.of(ChipColor.BLUE),
        ImmutableMultiset.of()
    ))
  }

  @Test
  fun moveReturnsChips() {
    val tookChips = game.takeChips(Player.ONE, ImmutableMultiset.builder<ChipColor>()
        .addCopies(ChipColor.GREEN, 4)
        .addCopies(ChipColor.RED, 3)
        .addCopies(ChipColor.WHITE, 2)
        .build())
    val returnedChips = tookChips.takeMove(
        TakeTokens(
            ImmutableMultiset.of(ChipColor.WHITE, ChipColor.RED, ChipColor.BLUE),
            ImmutableMultiset.of(ChipColor.GREEN, ChipColor.GREEN)
        )
    )
    assertChips(returnedChips.chips,
        5, 1, 3, 2, 0, 4)
    assertChips(returnedChips.tableaux[Player.ONE]!!.chips,
        0, 3, 1, 2, 4, 0)
  }

  @Test
  fun buyDevelopmentCard() {
    val gameWithChips = game.takeChips(
        Player.ONE, ImmutableMultiset.of(ChipColor.WHITE, ChipColor.WHITE, ChipColor.WHITE))
    val developmentCardMoves = gameWithChips.moves().filterIsInstance(BuyDevelopment::class.java)

    Truth.assertThat(developmentCardMoves).hasSize(1)
    val move = developmentCardMoves.single()
    Truth.assertThat(move.card.color).isEqualTo(Color.RED)
    Truth.assertThat(move.card.price).isEqualTo(
        ImmutableMultiset.of(Color.WHITE, Color.WHITE, Color.WHITE))
    Truth.assertThat(move.card.victoryPoints).isEqualTo(0)

    Truth.assertThat(move.price).containsExactly(ChipColor.WHITE, ChipColor.WHITE, ChipColor.WHITE)
    Truth.assertThat(move.noble).isNull()
  }

  @Test
  fun reserveDevelopmentCard() {
    val cardToReserve = DevelopmentCard(
        0,
        Color.RED,
        ImmutableMultiset.builder<Color>().addCopies(Color.WHITE, 3).build())
    val reserve = ReserveDevelopment(cardToReserve)
    val moves = game.moves()
    Truth.assertThat(moves).contains(reserve)
    Truth.assertThat(moves.filterIsInstance(ReserveDevelopment::class.java))
        .hasSize(OPEN_DEVELOPMENT_CARD_COUNT * 3)

    val gameWithReservedCard = game.takeMove(reserve)

    Truth.assertThat(gameWithReservedCard.chips).hasCount(ChipColor.GOLD, 4)

    val newRowOne = gameWithReservedCard.developments.cards(Row.ONE)
    Truth.assertThat(newRowOne).hasSize(4)
    Truth.assertThat(newRowOne).doesNotContain(cardToReserve)

    val newTableau = gameWithReservedCard.tableaux[Player.ONE]!!
    Truth.assertThat(newTableau.reservedDevelopments).containsExactly(cardToReserve)
    Truth.assertThat(newTableau.chips).containsExactly(ChipColor.GOLD)
  }
}
