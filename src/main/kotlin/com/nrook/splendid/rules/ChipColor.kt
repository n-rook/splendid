package com.nrook.splendid.rules

/**
 * The colors chips can be.
 */
enum class ChipColor(val databaseId: Int) {
  GREEN(0),
  BLUE(1),
  RED(2),
  WHITE(3),
  BLACK(4),
  GOLD(5);

  fun toColor(): Color {
    return COLOR_TO_CHIP_COLOR.inverse()[this] ?: throw Error("Cannot convert GOLD to chip")
  }
}
