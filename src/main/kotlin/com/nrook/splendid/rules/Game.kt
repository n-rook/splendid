package com.nrook.splendid.rules

import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap

/**
 * Game rules and state.
 */
data class Game(
    val turn: Turn,
    val developments: Developments,
    val nobles: ImmutableSet<Noble>,
    val chips: ImmutableMultiset<ChipColor>,
    val tableaux: ImmutableMap<Player, Tableau>,
    val decks: ImmutableMap<Row, Deck>
    );

fun setupNewGame(
    startingDevelopments: ImmutableSetMultimap<Row, DevelopmentCard>,
    remainingDevelopments: ImmutableListMultimap<Row, DevelopmentCard>,
    nobles: ImmutableSet<Noble>): Game {
  val decks: ImmutableMap<Row, Deck> = run {
    val builder = ImmutableMap.builder<Row, Deck>()
    for (row in Row.values()) {
      builder.put(row, Deck(remainingDevelopments[row]))
    }
    return@run builder.build()
  }

  return Game(
      Turn(1, Player.ONE),
      Developments(startingDevelopments),
      nobles,
      STARTING_CHIPS,
      ImmutableMap.of(Player.ONE, EMPTY_TABLEAU, Player.TWO, EMPTY_TABLEAU),
      decks
  )
}


val STARTING_CHIPS: ImmutableMultiset<ChipColor> = run {
  val builder: ImmutableMultiset.Builder<ChipColor> = ImmutableMultiset.builder()
  for (color in ChipColor.values()) {
    val count = if (color == ChipColor.GOLD) 5 else 4
    builder.addCopies(color, count)
  }
  builder.build()
}
