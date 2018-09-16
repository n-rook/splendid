package com.nrook.splendid.rules.testing

import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.Multiset
import com.google.common.truth.Truth
import com.nrook.splendid.rules.ChipColor

fun assertChips(chips: Multiset<ChipColor>, gold: Int, white: Int, blue: Int, green: Int, red: Int, black: Int) {
  Truth.assertThat(chips).isEqualTo(ImmutableMultiset.builder<ChipColor>()
      .addCopies(ChipColor.GOLD, gold)
      .addCopies(ChipColor.WHITE, white)
      .addCopies(ChipColor.BLUE, blue)
      .addCopies(ChipColor.GREEN, green)
      .addCopies(ChipColor.RED, red)
      .addCopies(ChipColor.BLACK, black)
      .build()
  )
}