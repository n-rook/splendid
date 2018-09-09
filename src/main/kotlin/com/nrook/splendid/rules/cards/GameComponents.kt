package com.nrook.splendid.rules.cards

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.ListMultimap
import com.nrook.splendid.rules.DevelopmentCard
import com.nrook.splendid.rules.Game
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

    val (startingCards, decks) = dealOutDevelopments(shuffledDevelopments)

    return setupNewGame(startingCards, decks, ImmutableSet.of()) // TODO nobles
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