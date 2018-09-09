package com.nrook.splendid.engine

import com.google.common.collect.ImmutableMap
import com.nrook.splendid.reporting.ConsoleReporter
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Player

class SelfPlayEngine(
    private val ais: ImmutableMap<Player, SynchronousAi>,
    private val reporter: ConsoleReporter) {

  /**
   * Run self-play.
   *
   * @return the victor.
   */
  fun run(initialState: Game): Player {
    var currentState: Game = initialState
    while (currentState.winner() == null) {
      val nextMove = ais[currentState.turn.player]!!.selectMove(currentState)
      reporter.reportMove(currentState, nextMove)
      currentState = currentState.takeMove(nextMove)
    }
    return currentState.winner()!!
  }
}
