package com.nrook.splendid.ai

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Maps
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.moves.Move
import java.util.*


/**
 * Chooses moves based on a scoring algorithm.
 */
class SimpleScorePlayer(
    val random: Random,
    val scorer: (state: Game) -> Double): SynchronousAi {

  override fun selectMove(game: Game): Move {
    val moves = game.moves()
    val moveToOutcome = Maps.toMap(moves) { game.takeMove(it!!) }
    val bestMoves = getMaxes(moves) { scorer(moveToOutcome[it]!!)}

    if (moves.isEmpty()) {
      throw Error("No legal moves. This should not be possible")
    }

    val chosenMove = bestMoves.asList()[random.nextInt(bestMoves.size)]
    return chosenMove
  }
}
