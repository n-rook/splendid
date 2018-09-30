package com.nrook.splendid.reporting

import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.moves.Move

class ConsoleShortReporter: Reporter {
  override fun describeGame(g: Game) {
    println("Turn ${g.turn}. ${g.tableaux[Player.ONE]!!.victoryPoints} to ${g.tableaux[Player.TWO]!!.victoryPoints}")
  }

  override fun reportMove(g: Game, m: Move) {
    describeGame(g)
  }
}
