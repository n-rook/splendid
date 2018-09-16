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
      is BuyDevelopment -> it.card.victoryPoints
      else -> 0
    } }

    val chosenMove = bestMoves.asList()[random.nextInt(bestMoves.size)]
    return chosenMove
  }
}

private fun <T> getMaxes(i: Iterable<T>, scorer: (v: T) -> Int): ImmutableSet<T> {
  var currentMax = Int.MIN_VALUE
  var currentMaxes = ArrayList<T>()
  for (v in i) {
    val score = scorer(v)
    if (score > currentMax) {
      currentMax = score
      currentMaxes.clear()
      currentMaxes.add(v)
    } else if (score == currentMax) {
      currentMaxes.add(v)
    }
  }
  return ImmutableSet.copyOf(currentMaxes)
}