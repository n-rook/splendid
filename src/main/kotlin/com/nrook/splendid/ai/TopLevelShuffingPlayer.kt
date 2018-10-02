package com.nrook.splendid.ai

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMultiset
import com.nrook.splendid.engine.RandomShuffler
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.moves.Move
import mu.KLogging
import java.util.*

/**
 * A player that takes randomness into account by delegating the problem to N different sub-players.
 */
class TopLevelShuffingPlayer(private val shuffler: RandomShuffler,
                             private val delegates: ImmutableList<SynchronousAi>): SynchronousAi {
  companion object {
    fun createFromCopies(random: Random, copies: Int, factory: () -> SynchronousAi):
        TopLevelShuffingPlayer {
      return TopLevelShuffingPlayer(
          RandomShuffler(random),
          ImmutableList.copyOf((1..copies).map { factory() }))
    }

    private val logger = KLogging().logger
  }

  override fun selectMove(game: Game): Move {
    val bestMoves = ImmutableMultiset.copyOf(delegates.map {
      val shuffledGame = shuffler.shuffle(game)
      it.selectMove(shuffledGame)
    })

    val mostPopularMove = bestMoves.entrySet().maxBy { it.count }!!
    logger.info {
      "Selecting most popular move (with count ${mostPopularMove.count} / ${delegates.size}"
    }
    return mostPopularMove.element
  }
}
