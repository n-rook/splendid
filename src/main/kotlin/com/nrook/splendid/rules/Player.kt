package com.nrook.splendid.rules

/**
 * Each player.
 *
 * Player 1 goes first.
 */
enum class Player {
  ONE,
  TWO;

  fun opponent(): Player {
    return when(this) {
      ONE -> Player.TWO
      TWO -> Player.ONE
    }
  }
}
