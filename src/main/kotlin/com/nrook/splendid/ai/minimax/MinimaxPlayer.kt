package com.nrook.splendid.ai.minimax

import com.google.common.collect.ImmutableList
import com.nrook.splendid.ai.ValueFunction
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.moves.BuyDevelopment
import com.nrook.splendid.rules.moves.Move
import mu.KLogging
import java.util.*

private val logger = KLogging().logger("Minimax")

/**
 * A player which uses a minimax algorithm to compute the best move.
 */
class MinimaxPlayer(val valueFunction: ValueFunction, private val ops: Int): SynchronousAi {
  override fun selectMove(game: Game): Move {
    val tree = MinimaxTree(game.turn.player, valueFunction, game)

    val expansionQueue = LinkedList<MinimaxTree.Node>(ImmutableList.of(tree.root))
    var depth = 0  // for logging only
    var currentDepthDenominator: Int = -1
    for (i in 0..ops) {
      if (expansionQueue.isEmpty()) {
        val toExpand = tree.root.getExpandableSelfOrDescendants()
        expansionQueue.addAll(toExpand)

        depth++
        logger.debug { "Expanding minimax tree for depth $depth" }
        currentDepthDenominator = toExpand.size
      }

      if (expansionQueue.isEmpty()) {
        // Good news: We have expanded the entire tree!
        break
      }

      val nextNode = expansionQueue.remove()
      nextNode.expand()
    }

    val choice = pickBestWinner(
        tree.root.bestEdges().map { it.edge })

    // If currentDepthDenominator is 0 we set remainingDepth to 1 because we didn't actually
    // do anything on this depth level.
    val remainingDepth: Double = if (currentDepthDenominator == 0) 1.0
    else expansionQueue.size.toDouble() / currentDepthDenominator
    describeChoice(choice, tree, depth - remainingDepth)

    return tree.root.bestEdge().edge
  }

  // Picks a winner from a set of equal-score options.
  // Not important for decision-making: the intent of this is just to stop the AI from
  // doing a touchdown dance when it has won the game, by encouraging it to take moves
  // which actually do something.
  private fun pickBestWinner(moves: List<Move>): Move {
    return moves.firstOrNull { it is BuyDevelopment } ?: moves.first()
  }

  private fun describeChoice(choice: Move, tree: MinimaxTree, depth: Double) {
    logger.debug { "Depth: $depth" }
    val choice = tree.root.bestEdge()

    if (choice.node.score == Double.POSITIVE_INFINITY) {
      logger.debug("${choice.edge} makes me win")
      return
    } else if (choice.node.score == Double.NEGATIVE_INFINITY) {
      logger.debug("Basically I'm screwed lol")
      return
    }

    val otherGoodChoices = tree.root.children!!.filter {
      choice.node.score - it.node.score < 100
    }
    if (otherGoodChoices.isEmpty()) {
      logger.debug("Went with choice ${choice.edge}. All the other choices were bad")
    } else {
      logger.debug("Went with choice ${choice.edge} (${choice.node.score}). But these choices were also good:\n")
      for (choice in otherGoodChoices) {
        logger.debug("${choice.node.score} | ${choice.edge}")
      }
    }
  }
}

private class MinimaxTree(val me: Player, val valueFunction: ValueFunction, game: Game) {
  val root: Node

  init {
    root = Node(game, null, null)
  }

  inner class Node(
      val state: Game,
      val parent: Node?,
      var children: ImmutableList<ChildRelationship>?) {
    val valueFunctionScore: Double = valueFunction.computeForPlayer(state, me)

    var scoreFromChildren: Double? = null

    val score: Double
      get() {
        return scoreFromChildren ?: valueFunctionScore
      }

    /**
     * Whether to compute the maximum or minimum here.
     */
    val nodeType: NodeType
      get() {
        return if (state.turn.player == me) {
          NodeType.MAX_NODE
        } else {
          NodeType.MIN_NODE
        }
      }

    val gameOver: Boolean = state.winner() != null

    fun bestEdge(): ChildRelationship {
      if (children == null) {
        throw Error("Children is null")
      }

      return when (nodeType) {
        NodeType.MAX_NODE -> {
          children!!.maxBy { it.node.score }!!
        }
        NodeType.MIN_NODE -> {
          children!!.minBy { it.node.score  }!!
        }
      }
    }

    /**
     * Returns all entries with the maximum score. Not very efficient.
     */
    fun bestEdges(): ImmutableList<ChildRelationship> {
      if (children == null) {
        throw Error("Children is null")
      }
      val maxScore = bestEdge().node.score
      return ImmutableList.copyOf(
          children!!.filter { it.node.score == maxScore }
      )
    }

    // Compute children, and get score based on them.
    fun expand() {
      if (gameOver) {
        throw Error("Cannot expand node; rootState is over")
      }

      val childrenBuilder = ImmutableList.builder<ChildRelationship>()
      for (move in state.moves()) {
        childrenBuilder.add(ChildRelationship(move, Node(state.takeMove(move), this, null)))
      }
      children = childrenBuilder.build()
//      println("${children!!.size} children")

      recalculateScore()
    }

    /**
     * Recalculate the scores of all of our ancestors.
     */
    fun recalculateAncestralScore() {
      if (parent != null) {
        parent.recalculateScore()
      }
    }

    /**
     * Recalculate our score from our children.
     *
     * Also recalculates our ancestral score.
     */
    fun recalculateScore() {
      if (children == null) {
        throw Error("Children is null")
      }

      if (children!!.isEmpty()) {
        throw Error("Children is empty. This should never happen because you can't call " +
            "expand on leaf nodes")
      }

      scoreFromChildren = bestEdge().node.score

      recalculateAncestralScore()
    }

    /**
     * If this node is expandable, return this node.
     *
     * Otherwise, return all expandable children.
     */
    fun getExpandableSelfOrDescendants(): ImmutableList<Node> {
      if (gameOver) {
        return ImmutableList.of()
      }

      if (children == null) {
        // We haven't been expanded yet.
        return ImmutableList.of(this)
      }

      // Ask our children.
      return ImmutableList.copyOf(children!!.flatMap { it.node.getExpandableSelfOrDescendants() })
    }
  }

  class ChildRelationship(val edge: Move, val node: MinimaxTree.Node)
}


private enum class NodeType {
  /**
   * On this node, it is our turn, so we should try to maximize our score.
   */
  MAX_NODE,

  /**
   * On this node, it is the opponent's turn, so we should try to minimize our score.
   */
  MIN_NODE
}
