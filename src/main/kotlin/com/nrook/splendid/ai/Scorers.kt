package com.nrook.splendid.ai

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.moves.Move
import java.util.*

// A scorer computes a score representing Player 1's chances of victory.

//val victoryPoints = symmetricScorer(
//    {game, player -> game.tableaux[player]!!.victoryPoints.toDouble() })

/**
 * From a single-player scoring function defines a multiplayer scoring function.
 */
fun symmetricScorer(singlePlayerScorer: (game: Game, player: Player) -> Double):
    (game: Game) -> Double {
  return { singlePlayerScorer(it, Player.ONE) - singlePlayerScorer(it, Player.TWO) }
}

/**
 * Takes the move which results in a position with the highest value.
 */
class DirectValuePlayer(
    private val random: Random,
    private val valueFunction: ValueFunction): SynchronousAi {

  override fun selectMove(game: Game): Move {
    val moves = game.moves()
    val moveToOutcome = Maps.toMap(moves) { game.takeMove(it!!) }
    val bestMoves = getMaxes(moves) {
      valueFunction.computeForPlayer(moveToOutcome[it]!!, game.turn.player)
    }

    if (moves.isEmpty()) {
      throw Error("No legal moves. This should not be possible")
    }

    return bestMoves.asList()[random.nextInt(bestMoves.size)]
  }
}

class ValueFunction(val weights: ImmutableMap<Feature, Double>) {

  /**
   * Determine a score for this game.
   *
   * +inf is returned if Player 1 has won, and -inf if they have lost.
   */
  fun compute(game: Game): Double {
    if (game.winner() != null) {
      return when(game.winner()) {
        Player.ONE -> Double.POSITIVE_INFINITY
        Player.TWO -> Double.NEGATIVE_INFINITY
        null -> throw Error("not possible")
      }
    }

    val values = computeAllFeatureValues(game)
    return weights.entries.sumByDouble { it.value * values[it.key]!! }
  }

  /**
   * Returns the score for this game, relative to this player.
   *
   * In other words, +inf is always a win for the given player.
   */
  fun computeForPlayer(game: Game, player: Player): Double {
    return compute(game) * when (player) {
      Player.ONE -> 1
      Player.TWO -> -1
    }
  }
}

/**
 * Compute the weights of all features.
 *
 * @return an immutable map of each feature to its weight.
 */
fun computeAllFeatureValues(game: Game): ImmutableMap<Feature, Double> {
  val weights = ImmutableMap.builder<Feature, Double>()
  for (feature in PlayerSpecificFeature.values()) {
    val summedWeight = feature.run(game, Player.ONE) - feature.run(game, Player.TWO)
    weights.put(feature, summedWeight)
  }
  for (feature in GeneralFeature.values()) {
    weights.put(feature, feature.run(game))
  }
  return weights.build()
}

//class FeatureWeights(val featuresToWeights: ImmutableMap<Feature, Double>) {
//  fun computedWeightedScore(values: ImmutableMap<Feature, Double>) {
//
//  }
//}

//
//class ScorerFactory(
//    val scorers: ImmutableMap<PlayerSpecificFeature, (game: Game, player: Player) -> Double>
//)

interface Feature

enum class PlayerSpecificFeature: Feature {
  /**
   * How many victory points each player has.
   */
  VICTORY_POINTS {
    override fun run(game: Game, player: Player): Double {
      return scoreVictoryPoints(game, player)
    }
  },
  DEVELOPMENTS {
    override fun run(game: Game, player: Player): Double {
      return scoreDevelopmentCount(game, player)
    }
  },
  CHIPS {
    override fun run(game: Game, player: Player): Double {
      return scoreChips(game, player)
    }
  };

  abstract fun run(game: Game, player: Player): Double
}

enum class GeneralFeature: Feature {
  SECOND_PLAYER_EXTRA_TURN_BONUS {
    override fun run(game: Game): Double {
      return if (game.turn.player == Player.TWO) 1.0 else 0.0
    }
  };


  /**
   * Returns the value of the feature.
   *
   * Positive values favor player 1, and negative ones favor player 2.
   */
  abstract fun run(game: Game): Double
}

fun scoreVictoryPoints(game: Game, player: Player): Double {
  return game.tableaux[player]!!.victoryPoints.toDouble()
}

fun scoreDevelopmentCount(game: Game, player: Player): Double {
  return game.tableaux[player]!!.developments.size.toDouble()
}

fun scoreChips(game: Game, player: Player): Double {
  return game.tableaux[player]!!.chips.size.toDouble()
}
