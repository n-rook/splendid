package com.nrook.splendid.ai.depthfirst

import com.nrook.splendid.ai.ValueFunction
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.moves.Move
import mu.KLogging
import java.lang.RuntimeException

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
      if (state.winner() != null) {
        return if (state.winner() == me) {
          EvaluationOutcome(null, Double.POSITIVE_INFINITY, true)
        } else {
          EvaluationOutcome(null, Double.NEGATIVE_INFINITY, true)
        }
      }

      if (remainingDepth > 0) {
        return expand()
      } else {
        return EvaluationOutcome(null, evaluate(), false)
      }
    }

    fun expand(): EvaluationOutcome {
      return when (this.nodeType) {
        NodeType.MAX_NODE -> expandMax()
        NodeType.MIN_NODE -> expandMin()
      }
    }

    // Find the move with the highest score.
    fun expandMax(): EvaluationOutcome {
      var bestMove: Move? = null
      var bestScore = Double.NEGATIVE_INFINITY
      var isPerfect = true

      for ((move, outcome) in movesAndOutcomes()) {
        val node = Node(outcome, remainingDepth - 1)
        val evaluation = node.computeScore()
        val nodeScore = evaluation.score
        isPerfect = isPerfect && evaluation.perfect
        if (nodeScore >= bestScore) {
          bestScore = nodeScore
          bestMove = move
        }
      }

      return EvaluationOutcome(bestMove!!, bestScore, isPerfect)
    }

    // Find the move with the lowest score.
    fun expandMin(): EvaluationOutcome {
      var bestMove: Move? = null
      var bestScore = Double.POSITIVE_INFINITY
      var isPerfect = true

      for ((move, outcome) in movesAndOutcomes()) {
        val node = Node(outcome, remainingDepth - 1)
        val evaluation = node.computeScore()
        val nodeScore = evaluation.score
        isPerfect = isPerfect && evaluation.perfect
        if (nodeScore <= bestScore) {
          bestScore = nodeScore
          bestMove = move
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
      return state.moves().map { MoveAndOutcome(it, state.takeMove(it)) }
    }
  }
}

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
