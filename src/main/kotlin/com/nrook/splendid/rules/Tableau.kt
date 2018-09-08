package com.nrook.splendid.rules

import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.ImmutableSet

/**
 * A single player's game area.
 *
 * The tableau consists of a player's chips, their development cards, their nobles, and any
 * development cards they have reserved.
 */
data class Tableau(
    val chips: ImmutableMultiset<ChipColor>,
    val developments: ImmutableSet<DevelopmentCard>,
    val nobles: ImmutableSet<Noble>,
    val reservedDevelopments: ImmutableSet<DevelopmentCard>) {
  val victoryPoints
    get() = developments.map { it.victoryPoints }.sum() +
        nobles.map { it.victoryPoints }.sum()
}

val EMPTY_TABLEAU = Tableau(
    ImmutableMultiset.of(),
    ImmutableSet.of(),
    ImmutableSet.of(),
    ImmutableSet.of()
)
