package com.nrook.splendid.rules

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.Iterables
import com.google.common.collect.Iterators

/**
 * The center of the table, where development cards are available to buy.
 */
data class Developments(
    val rows: ImmutableMap<Row, DevelopmentRow>) {

  /**
   * Iterates through development cards in order.
   */
  fun cards(): Iterable<DevelopmentCard> {
    return Iterables.concat(rows.values.map { it.cards })
  }

  fun removeCard(card: DevelopmentCard): Developments {
    val builder = ImmutableMap.builder<Row, DevelopmentRow>()
    var replaced = false
    for (r in Row.values()) {
      val oldRow = rows[r]!!
      val newRow: DevelopmentRow
      if (oldRow.cards.contains(card)) {
        replaced = true
        newRow = oldRow.removeCard(card)
      } else {
        newRow = oldRow
      }

      builder.put(r, newRow)
    }

    if (!replaced) {
      throw Error("Card $card not found in developments")
    }
    return Developments(builder.build())
  }
}

data class DevelopmentRow(val cards: ImmutableSet<DevelopmentCard>, val deck: Deck) {

  /**
   * Remove a card and draw a replacement.
   */
  fun removeCard(card: DevelopmentCard): DevelopmentRow {
    val remainder = Iterables.filter(cards) { it != card}
    if (deck.isEmpty()) {
      return DevelopmentRow(ImmutableSet.copyOf(remainder), deck)
    }

    val drawnCardResult = deck.draw()
    return DevelopmentRow(
        ImmutableSet.builder<DevelopmentCard>()
          .addAll(remainder)
          .add(drawnCardResult.card)
          .build(),
        drawnCardResult.rest)
  }
}
