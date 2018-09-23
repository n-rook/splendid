package com.nrook.splendid.rules

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets

/**
 * A deck of development cards.
 *
 * For simplicity, right now, decks are deterministic. The card at the end of the list is the
 * card on top of the deck.
 */
data class Deck(val cards: ImmutableList<DevelopmentCard>) {

  val cardsSet: ImmutableSet<DevelopmentCard> by lazy { ImmutableSet.copyOf(cards) }

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

  /**
   * Returns a deck consisting of the same cards, but in the order given below.
   *
   * Throws if the new order doesn't have the same cards as the old one.
   */
  fun reorder(newOrder: Iterable<DevelopmentCard>): Deck {

    // Turn this off if it's slow...

    val newContents = ImmutableSet.copyOf(newOrder)
    if (newContents != cardsSet) {
      val newCards = Sets.difference(newContents, cardsSet)
      val missingCards = Sets.difference(cardsSet, newContents)
      throw Error("New ordering has different cards\nNew:$newCards\nMissing:$missingCards")
    }

    return Deck(ImmutableList.copyOf(newOrder))

  }
}

data class DrawnCardResult(val card: DevelopmentCard, val rest: Deck)
