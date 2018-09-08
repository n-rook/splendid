package com.nrook.splendid.rules

import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.Iterators

/**
 * The center of the table, where development cards are available to buy.
 */
data class Developments(val rows: ImmutableSetMultimap<Row, DevelopmentCard>): Iterable<DevelopmentCard> {
  /**
   * Iterates through development cards in order.
   */
  override fun iterator(): Iterator<DevelopmentCard> {
    return Iterators.concat(Row.values().map { rows.get(it).iterator() }.iterator())
  }

  fun replaceRow(row: Row, cards: Iterable<DevelopmentCard>): Developments {
    val builder = ImmutableSetMultimap.builder<Row, DevelopmentCard>()
    for (r in Row.values()) {
      builder.putAll(r, if (r == row) cards else rows[r])
    }

    return Developments(builder.build())
  }
}
