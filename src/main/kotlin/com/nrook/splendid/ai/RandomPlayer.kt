package com.nrook.splendid.ai

import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.moves.Move
import java.util.*

/**
 * Chooses moves entirely randomly.
 */
class RandomPlayer(val random: Random): SynchronousAi {
  override fun selectMove(game: Game): Move {
    val moves = game.moves()
    val chosenMove = moves[random.nextInt(moves.size)]
    return chosenMove
  }
}