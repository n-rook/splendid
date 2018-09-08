package com.nrook.splendid.rules

import com.google.common.collect.ImmutableList

/**
 * A deck of development cards.
 *
 * For simplicity, right now, decks are deterministic. The card at the end of the list is the
 * card on top of the deck.
 */
data class Deck(val cards: ImmutableList<DevelopmentCard>) {
  fun draw(): DrawnCardResult {
    if (cards.size == 0) {
      throw Error("Deck is empty, cannot draw cards")
    }
    return DrawnCardResult(
        cards[cards.size - 1],
        Deck(cards.subList(0, cards.size - 1))
    )
  }

  fun isEmpty(): Boolean {
    return cards.isEmpty()
  }
}

data class DrawnCardResult(val card: DevelopmentCard, val rest: Deck)
