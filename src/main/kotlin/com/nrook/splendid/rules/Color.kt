package com.nrook.splendid.rules

import com.google.common.collect.ImmutableBiMap

/**
 * The colors required to buy resources.
 */
enum class Color {
  GREEN,
  BLUE,
  RED,
  WHITE,
  BLACK;

  fun toChipColor(): ChipColor {
    return COLOR_TO_CHIP_COLOR[this]!!
  }
}

val COLOR_TO_CHIP_COLOR = ImmutableBiMap.of<Color, ChipColor>(
    Color.GREEN, ChipColor.GREEN,
    Color.BLUE, ChipColor.BLUE,
    Color.RED, ChipColor.RED,
    Color.WHITE, ChipColor.WHITE,
    Color.BLACK, ChipColor.BLACK)
