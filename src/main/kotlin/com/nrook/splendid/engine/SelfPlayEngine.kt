package com.nrook.splendid.engine

import com.google.common.collect.ImmutableMap
import com.nrook.splendid.reporting.ConsoleReporter
import com.nrook.splendid.reporting.Reporter
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Player

class SelfPlayEngine(
    private val ais: ImmutableMap<Player, SynchronousAi>,
    private val shuffler: Shuffler,
    private val reporter: Reporter,
    private val cutoff: Int?) {

  /**
   * Run self-play.
   *
   * @return the victor.
   */
  fun run(initialState: Game): Player {
    var currentState: Game = initialState
    while (currentState.winner() == null) {
      if (cutoff != null && currentState.turn.index > cutoff) {
        // This game was a fiasco. Cutting off early and making an arbitrary player win.
        return Player.ONE
      }

      val nextMove = ais[currentState.turn.player]!!.selectMove(currentState)
      reporter.reportMove(currentState, nextMove)
      // Shuffle after the AI chooses a move, but before the move actually happens, so that
      // it cannot make use of the information here.
      currentState = shuffler.shuffle(currentState).takeMove(nextMove)
    }
    return currentState.winner()!!
  }
}
