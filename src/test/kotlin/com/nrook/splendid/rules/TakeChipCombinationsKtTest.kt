package com.nrook.splendid.rules

import com.google.common.collect.ImmutableMultiset
import com.google.common.truth.Truth
import org.junit.Test

class TakeChipCombinationsKtTest {

  @Test
  fun canGiveBackDifferentChips() {
    val result = takeDifferentChips(
        ImmutableMultiset.of(ChipColor.GREEN),
        ImmutableMultiset.of(ChipColor.RED, ChipColor.BLUE, ChipColor.WHITE),
        1,
        1)
    Truth.assertThat(result).containsExactly(
      TakeChipsResults(ImmutableMultiset.of(), ImmutableMultiset.of()),
      TakeChipsResults(
        ImmutableMultiset.of(ChipColor.GREEN), ImmutableMultiset.of(ChipColor.RED)),
      TakeChipsResults(
          ImmutableMultiset.of(ChipColor.GREEN), ImmutableMultiset.of(ChipColor.BLUE)),
      TakeChipsResults(
          ImmutableMultiset.of(ChipColor.GREEN), ImmutableMultiset.of(ChipColor.WHITE))
    )
  }

  @Test
  fun regression() {
    val result = takeDifferentChips(
        ImmutableMultiset.Builder<ChipColor>()
            .addCopies(ChipColor.BLUE, 4)
            .addCopies(ChipColor.RED, 1)
            .addCopies(ChipColor.WHITE, 2)
            .addCopies(ChipColor.BLACK, 4)
            .build(),
        ImmutableMultiset.Builder<ChipColor>()
            .addCopies(ChipColor.GREEN, 4)
            .addCopies(ChipColor.RED, 3)
            .addCopies(ChipColor.WHITE, 2)
            .build(),
        2,
        1)
    Truth.assertThat(result).contains(TakeChipsResults(
        ImmutableMultiset.of(ChipColor.BLUE, ChipColor.BLACK),
        ImmutableMultiset.of(ChipColor.RED)
    ))
  }
}
