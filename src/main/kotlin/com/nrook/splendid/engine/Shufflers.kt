package com.nrook.splendid.engine

import com.google.common.collect.ImmutableMap
import com.nrook.splendid.rules.DevelopmentRow
import com.nrook.splendid.rules.Developments
import com.nrook.splendid.rules.Game
import java.util.*
import kotlin.collections.ArrayList

interface Shuffler {
  /**
   * Introduce randomness to the game's decks.
   */
  fun shuffle(game: Game): Game
}

/**
 * A shuffler that actually shuffles the deck.
 */
class RandomShuffler(private val random: Random): Shuffler {
  override fun shuffle(game: Game): Game {
    val newRows = game.developments.rows.mapValues {
      val deck = it.value.deck
      val newOrder = ArrayList(deck.cards)
      newOrder.shuffle(random)
      DevelopmentRow(it.value.cards, deck.reorder(newOrder))
    }
    return Game(
        game.turn,
        Developments(ImmutableMap.copyOf(newRows)),
        game.nobles,
        game.chips,
        game.tableaux
    )
  }
}

/**
 * A shuffler that does nothing.
 */
class NullShuffler: Shuffler {
  override fun shuffle(game: Game): Game {
    return game
  }
}
