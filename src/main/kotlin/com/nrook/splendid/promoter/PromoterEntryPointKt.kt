package com.nrook.splendid.promoter

import com.google.common.collect.ImmutableMap
import com.nrook.splendid.ai.DirectValuePlayer
import com.nrook.splendid.ai.GeneralFeature
import com.nrook.splendid.ai.GreedyRandomPlayer
import com.nrook.splendid.ai.PlayerSpecificFeature
import com.nrook.splendid.ai.ValueFunction
import java.util.*

fun main(args: Array<String>) {
  val random = Random()
  val playerOneAi = GreedyRandomPlayer(random)
  val playerTwoAi = DirectValuePlayer(
      random,
      ValueFunction(
          ImmutableMap.of(
              PlayerSpecificFeature.VICTORY_POINTS, 100.0,
              PlayerSpecificFeature.DEVELOPMENTS, 10.0,
              PlayerSpecificFeature.CHIPS, 1.0,
              GeneralFeature.SECOND_PLAYER_EXTRA_TURN_BONUS, 5.0
          )
      )
  )
  winRate(playerOneAi, playerTwoAi, 100)
}
