package com.nrook.splendid.reporting

import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.moves.Move

/**
 * A reporter which reports nothing.
 */
class NullReporter: Reporter {
  override fun describeGame(g: Game) {
  }

  override fun reportMove(g: Game, m: Move) {
  }
}