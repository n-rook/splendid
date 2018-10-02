package com.nrook.splendid.ai.depthfirst

import com.google.common.collect.ImmutableMap
import com.nrook.splendid.ai.ValueFunction
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.moves.BuyDevelopment
import com.nrook.splendid.rules.moves.DoNothing
import com.nrook.splendid.rules.moves.Move
import com.nrook.splendid.rules.moves.ReserveDevelopment
import com.nrook.splendid.rules.moves.TakeTokens
import mu.KLogging
import java.lang.RuntimeException
import kotlin.math.max
import kotlin.math.min

private val logger = KLogging().logger("Depth-First ID Minimax")

class TimeBombValueFunction(private val valueFunction: ValueFunction, private var remainingOps: Int) {
  fun compute(game: Game): Double {
    tick()
    return valueFunction.compute(game)
  }

  fun computeForPlayer(game: Game, player: Player): Double {
    tick()
    return valueFunction.computeForPlayer(game, player)
  }

  private fun tick() {
    remainingOps--
    if (remainingOps <= 0) {
      throw TimeBombException()
    }
  }
}

class TimeBombException: RuntimeException("Time ran out")

class DepthFirstMinimaxPlayer(val valueFunction: ValueFunction, private val ops: Int): SynchronousAi {
  override fun selectMove(game: Game): Move {
    var mostRecentFullEvaluation: EvaluationOutcome? = null
    val timeBombValueFunction = TimeBombValueFunction(valueFunction, ops)
    try {
      // Iterative deepening
      for (depth in 1..Int.MAX_VALUE) {
        val root = MoveEvaluation(game.turn.player, timeBombValueFunction, game, depth)
        mostRecentFullEvaluation = root.evaluate()
        logger.debug { "Just finished evaluating at depth $depth." +
            " Best move had score ${mostRecentFullEvaluation.score}" }
        logger.debug { "Best move: ${mostRecentFullEvaluation.move}" }

        if (mostRecentFullEvaluation.perfect) {
          logger.debug { "Got perfect results at depth $depth, done evaluating" }
          break
        }
      }
    } catch (e: TimeBombException) {
      logger.debug { "Stopped evaluating." }
    }

    if (mostRecentFullEvaluation == null) {
      logger.warn { "Unable to complete a single round of evaluation" }
      return game.moves()[0]
    }

    return mostRecentFullEvaluation!!.move!!
  }
}

/**
 * @param depth The depth to evaluate. 1 means evaluate the next moves (1 ply), etc.
 */
class MoveEvaluation(
    val me: Player,
    val valueFunction: TimeBombValueFunction,
    val rootState: Game,
    val depth: Int) {
  val root: Node

  init {
    root = Node(rootState, depth)
  }

  fun evaluate(): EvaluationOutcome {
    return root.computeScore()
  }

  inner class Node(val state: Game, val remainingDepth: Int) {

    val nodeType get() = if (state.turn.player == me) NodeType.MAX_NODE else NodeType.MIN_NODE

    /**
     * Compute score. May construct child nodes. Or not.
     */
    fun computeScore(): EvaluationOutcome {
      return computeScore(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
    }

    fun computeScore(alpha: Double, beta: Double): EvaluationOutcome {
      if (state.winner() != null) {
        return if (state.winner() == me) {
          EvaluationOutcome(null, Double.POSITIVE_INFINITY, true)
        } else {
          EvaluationOutcome(null, Double.NEGATIVE_INFINITY, true)
        }
      }

      if (remainingDepth > 0) {
        return expand(alpha, beta)
      } else {
        return EvaluationOutcome(null, evaluate(), false)
      }
    }

    /**
     * Expand.
     *
     * On a max node, the following rules apply:
     *
     * "beta" is the upper bound for scores. Beta is the minimum score we've found at a sibling
     * node. If this node actually has a score higher than beta, the parent node (which is a min
     * node) will choose that other node with beta as its child, rather than choosing this node,
     * so we can return early.
     *
     * "alpha" is the lower bound for scores. On a max node, we don't use alpha directly. Rather, we
     * keep track of the maximum score we see in our child nodes. We pass that to our children
     * when they are evaluated, and call it alpha.
     *
     * (Actually, sometimes alpha is set in a previous max node, and gets passed in here. In this
     * case, it can cut off a child min node's computation from far away.)
     *
     * On a min node, the reverse rules apply. We directly use "alpha", which is the highest score
     * we've seen from a child at the parent max node. If we see that this node will evaluate to a
     * score lower than alpha, we know the parent will just use the node with alpha as its score
     * instead, so we return early.
     *
     * And similarly, we keep track of beta as the maximum score we see in our children. Our
     * children know that if they have a score higher than beta, there is no point in continuing
     * to evaluate, because we will just choose the beta node.
     *
     */
    fun expand(alpha: Double, beta: Double): EvaluationOutcome {
      return when (this.nodeType) {
        NodeType.MAX_NODE -> expandMax(alpha, beta)
        NodeType.MIN_NODE -> expandMin(alpha, beta)
      }
    }

    // Find the move with the highest score.
    fun expandMax(alpha: Double, beta: Double): EvaluationOutcome {
      var bestMove: Move? = null
      var bestScore = Double.NEGATIVE_INFINITY
      var isPerfect = true

      for ((move, outcome) in movesAndOutcomes()) {
        val node = Node(outcome, remainingDepth - 1)
        val evaluation = node.computeScore(
            max(alpha, bestScore),
            beta
        )
        val nodeScore = evaluation.score
        isPerfect = isPerfect && evaluation.perfect

        if (nodeScore >= bestScore) {
          bestScore = nodeScore
          bestMove = move
        }

        // Beta cutoff. An ancestor min node knows about a route that leads to a score of beta,
        // so there's no way it's going to choose this node.
        if (bestScore >= beta) {
          logger.trace { "Beta cutoff: best score $bestScore is greater than $beta" }
           return EvaluationOutcome(bestMove!!, bestScore, isPerfect)
        }
      }

      return EvaluationOutcome(bestMove!!, bestScore, isPerfect)
    }

    // Find the move with the lowest score.
    fun expandMin(alpha: Double, beta: Double): EvaluationOutcome {
      var bestMove: Move? = null
      var bestScore = Double.POSITIVE_INFINITY
      var isPerfect = true

      for ((move, outcome) in movesAndOutcomes()) {
        val node = Node(outcome, remainingDepth - 1)
        val evaluation = node.computeScore(
            alpha,
            min(beta, bestScore)
        )
        val nodeScore = evaluation.score
        isPerfect = isPerfect && evaluation.perfect

        if (nodeScore <= bestScore) {
          bestScore = nodeScore
          bestMove = move
        }

        // Alpha cutoff. An ancestor max node knows about a route that leads to a score of alpha,
        // so there's no way it's going to choose this node, which has a score less than alpha.
        if (bestScore <= alpha) {
          logger.trace { "Alpha cutoff: best score $bestScore is less than $alpha" }
          return EvaluationOutcome(bestMove!!, bestScore, isPerfect)
        }
      }

      return EvaluationOutcome(bestMove!!, bestScore, isPerfect)
    }

    /**
     * Just evaluate the state here, don't look at depth.
     */
    fun evaluate(): Double {
      return valueFunction.computeForPlayer(state, me)
    }

    fun movesAndOutcomes(): Iterable<MoveAndOutcome> {
      // Smart ordering!
      // In general, buying development moves are the best.
      // This also gets it away from styling on the opponent.

      return state.moves().sortedByDescending {
        MOVE_TYPE_PRIORITY[it::class.java]
      }.map { MoveAndOutcome(it, state.takeMove(it)) }
    }
  }
}

private val MOVE_TYPE_PRIORITY = ImmutableMap.of<Class<*>, Int>(
    TakeTokens::class.java, 2,
    ReserveDevelopment::class.java, 1,
    BuyDevelopment::class.java, 3,
    DoNothing::class.java, 0)

data class MoveAndOutcome(val move: Move, val outcome: Game)

// Contains the best move and its score.
// The move is null if we decide not to evaluate it.
/**
 * Contains the best move and its score.
 *
 * @param perfect Whether or not we evaluated the entire tree.
 */
data class EvaluationOutcome(val move: Move?, val score: Double, val perfect: Boolean)

enum class NodeType {
  /**
   * On this node, it is our turn, so we should try to maximize our score.
   */
  MAX_NODE,

  /**
   * On this node, it is the opponent's turn, so we should try to minimize our score.
   */
  MIN_NODE
}
