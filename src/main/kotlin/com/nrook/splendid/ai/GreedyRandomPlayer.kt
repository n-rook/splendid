package com.nrook.splendid.ai

import com.google.common.collect.ImmutableSet
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.moves.BuyDevelopment
import com.nrook.splendid.rules.moves.Move
import java.util.*
import kotlin.collections.ArrayList

/**
 * Chooses moves randomly, preferring those which get lots of victory points.
 */
class GreedyRandomPlayer(val random: Random): SynchronousAi {
  override fun selectMove(game: Game): Move {
    val moves = game.moves()
    val bestMoves = getMaxes(moves) { when(it) {
      is BuyDevelopment -> it.card.victoryPoints.toDouble()
      else -> 0.0
    } }
    if (moves.isEmpty()) {
      throw Error("No legal moves. This should not be possible")
    }

    val chosenMove = bestMoves.asList()[random.nextInt(bestMoves.size)]
    return chosenMove
  }
}
