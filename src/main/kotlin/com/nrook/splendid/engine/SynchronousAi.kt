package com.nrook.splendid.engine

import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.moves.Move

interface SynchronousAi {
  /**
   * Choose which move to make.
   */
  fun selectMove(game: Game): Move
}
