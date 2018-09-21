package com.nrook.splendid.rules.cards

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.ListMultimap
import com.nrook.splendid.rules.DevelopmentCard
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.NOBLE_COUNT
import com.nrook.splendid.rules.Noble
import com.nrook.splendid.rules.OPEN_DEVELOPMENT_CARD_COUNT
import com.nrook.splendid.rules.Row
import com.nrook.splendid.rules.setupNewGame
import java.util.*
import kotlin.collections.ArrayList

data class GameComponents(
    val developments: ImmutableSetMultimap<Row, DevelopmentCard>,
    val nobles: ImmutableSet<Noble>) {

  fun startGame(random: Random): Game {

    val shuffledDevelopments = kotlin.run {
      val builder = ImmutableListMultimap.builder<Row, DevelopmentCard>()
      for (row in Row.values()) {
        builder.putAll(row, shuffle(developments[row], random))
      }
      builder.build()
    }
    val nobles = ImmutableSet.copyOf(shuffle(nobles, random).subList(0, NOBLE_COUNT))

    val (startingCards, decks) = dealOutDevelopments(shuffledDevelopments)

    return setupNewGame(startingCards, decks, nobles) // TODO nobles
  }

  /**
   * Starts a game with a fixed selection of pieces.
   *
   * This is deterministic: it always returns the same game given the same input.
   */
  fun startFixedGame(startingDevelopments: Collection<DevelopmentCard>,
                     nobles: Collection<Noble>): Game {
    val byRow = run {
      val builder = ImmutableSetMultimap.Builder<Row, DevelopmentCard>()
      for (d in startingDevelopments) {
        val r = developments.inverse()[d]
        when (r.size) {
          0 -> throw Error("Could not find development $d")
          1 -> builder.put(r.single(), d)
          else -> throw Error("Development $d appeared in ${r.size} rows")
        }
      }

      builder.build()
    }

    val decks = run {
      val builder = ImmutableListMultimap.Builder<Row, DevelopmentCard>()
      for (r in Row.values()) {
        val sorted = developments[r].filter { !byRow[r].contains(it) }
            .sorted()
        builder.putAll(r, sorted)
      }

      builder.build()
    }

    return setupNewGame(byRow, decks, ImmutableSet.copyOf(nobles))
  }
}

fun dealOutDevelopments(d: ListMultimap<Row, DevelopmentCard>): Pair<ImmutableSetMultimap<Row, DevelopmentCard>, ImmutableListMultimap<Row, DevelopmentCard>> {
  val startingCards = ImmutableSetMultimap.builder<Row, DevelopmentCard>()
  val decks = ImmutableListMultimap.builder<Row, DevelopmentCard>()
  for (row in Row.values()) {
    startingCards.putAll(row, d[row].subList(0, OPEN_DEVELOPMENT_CARD_COUNT))
    decks.putAll(row, d[row].subList(OPEN_DEVELOPMENT_CARD_COUNT, d[row].size))
  }
  return Pair(startingCards.build(), decks.build())
}

fun <E> shuffle(c: Collection<E>, r: Random): ImmutableList<E> {
  val shufflee = ArrayList(c)
  shufflee.shuffle(r)
  return ImmutableList.copyOf(shufflee)
}
