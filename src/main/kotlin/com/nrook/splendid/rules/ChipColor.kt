package com.nrook.splendid.rules

/**
 * The colors chips can be.
 */
enum class ChipColor {
  GREEN,
  BLUE,
  RED,
  WHITE,
  BLACK,
  GOLD;

  fun toColor(): Color {
    return COLOR_TO_CHIP_COLOR.inverse()[this] ?: throw Error("Cannot convert GOLD to chip")
  }
}
