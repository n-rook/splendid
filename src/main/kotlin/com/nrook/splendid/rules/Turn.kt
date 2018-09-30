package com.nrook.splendid.rules

/**
 * Whose turn it is. Index starts at zero.
 */
data class Turn(val index: Int, val player: Player) {
  companion object {
    val START = Turn(0, Player.ONE)
  }

  fun next(): Turn {
    return when (player) {
      Player.ONE -> Turn(index, Player.TWO)
      Player.TWO -> Turn(index + 1, Player.ONE)
    }
  }

  override fun toString(): String {
    return when(player) {
      Player.ONE -> index.toString()
      Player.TWO -> "${index}x"
    }
  }
}
