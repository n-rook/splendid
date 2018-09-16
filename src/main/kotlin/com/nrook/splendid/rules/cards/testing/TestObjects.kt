package com.nrook.splendid.rules.cards.testing

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.ImmutableSetMultimap
import com.nrook.splendid.rules.ChipColor
import com.nrook.splendid.rules.Color
import com.nrook.splendid.rules.DevelopmentCard
import com.nrook.splendid.rules.Game
import com.nrook.splendid.rules.Noble
import com.nrook.splendid.rules.Row
import com.nrook.splendid.rules.cards.loadComponentsFromFile

private val testStartingDevelopments: ImmutableList<DevelopmentCard> =
    ImmutableList.builder<DevelopmentCard>()
        // Row 1
        .add(createDevelopmentCard(0, Color.RED, Color.WHITE, 3))
        .add(createDevelopmentCard(1, Color.BLUE, Color.RED, 4))
        .add(createDevelopmentCard(
            0, Color.WHITE, Color.BLUE, 1, Color.GREEN, 2, Color.RED, 1, Color.BLACK, 1))
        .add(createDevelopmentCard(
            0, Color.GREEN, Color.WHITE, 1, Color.BLUE, 3, Color.GREEN, 1
        ))
        // Row 2
        .add(createDevelopmentCard(
            2, Color.GREEN, Color.WHITE, 4, Color.BLUE, 2, Color.BLACK, 1))
        .add(createDevelopmentCard(3, Color.BLACK, Color.BLACK, 6))
        .add(createDevelopmentCard(
            1, Color.RED, Color.BLUE, 3, Color.RED, 2, Color.BLACK, 3))
        .add(createDevelopmentCard(
            2, Color.BLACK, Color.GREEN, 5, Color.RED, 3))
        // Row 3
        .add(createDevelopmentCard(
            4, Color.BLUE, Color.WHITE, 6, Color.BLUE, 3, Color.BLACK, 3))
        .add(createDevelopmentCard(
            4, Color.BLACK, Color.RED, 7))
        .add(createDevelopmentCard(
            5, Color.BLUE, Color.WHITE, 7, Color.BLUE, 3))
        .add(createDevelopmentCard(
            3, Color.BLACK, Color.WHITE, 3, Color.BLUE, 3, Color.GREEN, 5, Color.RED, 3))
        .build()

private val testStartingNobles = ImmutableList.of(
    createNoble(3, Color.BLACK, Color.RED, Color.WHITE),
    createNoble(3, Color.BLACK, Color.WHITE),
    createNoble(3, Color.BLACK, Color.RED, Color.GREEN)
)

private val testGame: Game by lazy {
  loadComponentsFromFile().startFixedGame(testStartingDevelopments, testStartingNobles)
}

private fun createDevelopmentCard(vp: Int, color: Color, c1: Color, p1: Int): DevelopmentCard {
  val price = ImmutableMultiset.builder<Color>()
      .addCopies(c1, p1)
      .build()
  return DevelopmentCard(vp, color, price)
}

private fun createDevelopmentCard(vp: Int, color: Color, c1: Color, p1: Int, c2: Color, p2: Int):
    DevelopmentCard {
  val price = ImmutableMultiset.builder<Color>()
      .addCopies(c1, p1)
      .addCopies(c2, p2)
      .build()
  return DevelopmentCard(vp, color, price)
}

private fun createDevelopmentCard(vp: Int, color: Color, c1: Color, p1: Int, c2: Color, p2: Int,
                                  c3: Color, p3: Int): DevelopmentCard {
  val price = ImmutableMultiset.builder<Color>()
      .addCopies(c1, p1)
      .addCopies(c2, p2)
      .addCopies(c3, p3)
      .build()
  return DevelopmentCard(vp, color, price)
}

private fun createDevelopmentCard(vp: Int, color: Color, c1: Color, p1: Int, c2: Color, p2: Int,
                                  c3: Color, p3: Int, c4: Color, p4: Int): DevelopmentCard {
  val price = ImmutableMultiset.builder<Color>()
      .addCopies(c1, p1)
      .addCopies(c2, p2)
      .addCopies(c3, p3)
      .addCopies(c4, p4)
      .build()
  return DevelopmentCard(vp, color, price)
}

private fun createNoble(vp: Int, c1: Color, c2: Color): Noble {
  return Noble(vp,
      ImmutableMultiset.builder<Color>()
          .addCopies(c1, 4)
          .addCopies(c2, 4)
          .build())
}

private fun createNoble(vp: Int, c1: Color, c2: Color, c3: Color): Noble {
  return Noble(vp,
      ImmutableMultiset.builder<Color>()
          .addCopies(c1, 3)
          .addCopies(c2, 3)
          .addCopies(c3, 3)
          .build())
}

fun createStandardTestGame(): Game {
  return testGame
}