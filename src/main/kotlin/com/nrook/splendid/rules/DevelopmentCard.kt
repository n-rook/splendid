package com.nrook.splendid.rules

import com.google.common.collect.Multiset

/**
 * A development card to be collected.
 */
data class DevelopmentCard(val victoryPoints: Int, val color: Color, val price: Multiset<Color>)
