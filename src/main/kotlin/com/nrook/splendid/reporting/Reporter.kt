package com.nrook.splendid.reporting

import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.moves.Move

interface Reporter {
  fun describeGame(g: Game)
  fun reportMove(g: Game, m: Move)
}