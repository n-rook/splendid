package com.nrook.splendid.rules

import com.google.common.collect.ImmutableMultiset

data class Noble(val victoryPoints: Int, val requirements: ImmutableMultiset<Color>)
