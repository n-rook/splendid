package com.nrook.splendid.ai.minimax

import com.google.common.collect.ImmutableList
import com.nrook.splendid.ai.ValueFunction
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.moves.Move
import mu.KLogging
import java.util.*

private val OPS = 10000

val logger = KLogging().logger("Minimax")

/**
 * A player which uses a minimax algorithm to compute the best move.
 */
class MinimaxPlayer(val valueFunction: ValueFunction): SynchronousAi {
  override fun selectMove(game: Game): Move {
    val tree = MinimaxTree(game.turn.player, valueFunction, game)

    val expansionQueue = LinkedList<MinimaxTree.Node>(ImmutableList.of(tree.root))
    var depth = 0  // for logging only
    var currentDepthDenominator: Int = -1
    for (i in 0..OPS) {
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

    describeChoice(tree, depth - (expansionQueue.size.toDouble() / currentDepthDenominator))

    return tree.root.bestEdge().edge
  }

  fun describeChoice(tree: MinimaxTree, depth: Double) {
    logger.debug { "Depth: $depth" }
    val topChoice = tree.root.bestEdge()

    if (topChoice.node.score == Double.POSITIVE_INFINITY) {
      logger.debug("${topChoice.edge} makes me win")
      return
    } else if (topChoice.node.score == Double.NEGATIVE_INFINITY) {
      logger.debug("Basically I'm screwed lol")
      return
    }

    val otherGoodChoices = tree.root.children!!.filter {
      topChoice.node.score - it.node.score < 100
    }
    if (otherGoodChoices.isEmpty()) {
      logger.debug("Went with choice ${topChoice.edge}. All the other choices were bad")
    } else {
      logger.debug("Went with choice ${topChoice.edge} (${topChoice.node.score}). But these choices were also good:\n")
      for (choice in otherGoodChoices) {
        logger.debug("${choice.node.score} | ${choice.edge}")
      }
    }
  }
}

class MinimaxTree(val me: Player, val valueFunction: ValueFunction, game: Game) {
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

    // Compute children, and get score based on them.
    fun expand() {
      if (gameOver) {
        throw Error("Cannot expand node; game is over")
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
