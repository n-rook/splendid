package com.nrook.splendid.rules

import com.google.common.collect.ComparisonChain
import com.google.common.collect.Multiset

/**
 * A development card to be collected.
 */
data class DevelopmentCard(val victoryPoints: Int, val color: Color, val price: Multiset<Color>):
    Comparable<DevelopmentCard> {

  // Sometimes, it's convenient to be able to sort development cards.
  override fun compareTo(other: DevelopmentCard): Int {
    var chain = ComparisonChain.start()
        .compare(victoryPoints, other.victoryPoints)
        .compare(color, other.color)
    for (color in Color.values()) {
      chain = chain.compare(price.count(color), other.price.count(color))
    }
    return chain.result()
  }
}
