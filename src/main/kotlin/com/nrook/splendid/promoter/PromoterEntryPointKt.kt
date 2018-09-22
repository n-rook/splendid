package com.nrook.splendid.promoter

import com.google.common.collect.ImmutableMap
import com.nrook.splendid.ai.DirectValuePlayer
import com.nrook.splendid.ai.GeneralFeature
import com.nrook.splendid.ai.PlayerSpecificFeature
import com.nrook.splendid.ai.ValueFunction
import com.nrook.splendid.ai.minimax.MinimaxPlayer
import java.util.*

fun main(args: Array<String>) {
  val random = Random()
  val valueFunction = ValueFunction(
      ImmutableMap.of(
          PlayerSpecificFeature.VICTORY_POINTS, 100.0,
          PlayerSpecificFeature.DEVELOPMENTS, 10.0,  // won vs 100
          PlayerSpecificFeature.CHIPS, 1.0,
          GeneralFeature.SECOND_PLAYER_EXTRA_TURN_BONUS, 5.0
      )
  )
  val playerOneAi = DirectValuePlayer(
      random,
      valueFunction
  )
  val playerTwoAi = MinimaxPlayer(valueFunction)
  winRate(playerOneAi, playerTwoAi, 10)
}
