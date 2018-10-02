package com.nrook.splendid.promoter

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.nrook.splendid.ai.DirectValuePlayer
import com.nrook.splendid.ai.GeneralFeature
import com.nrook.splendid.ai.PlayerSpecificFeature
import com.nrook.splendid.ai.ValueFunction
import com.nrook.splendid.ai.minimax.MinimaxPlayer
import java.util.*

fun main(args: Array<String>) {
//  winRate(MINIMAX_10K_MARK_1_VF1.ai, TEMP_DFS_ALPHA_BETA.ai, 10)
  doSelfPlayForever()
}

fun doSelfPlayForever() {
  selfPlayForever(ImmutableList.of(
//      RANDOM_PLAYER,
      DIRECT_VF1,
      MINIMAX_1K_MARK_1_VF1,
      MINIMAX_10K_MARK_1_VF1,
      X10_SHUFFLER_MINIMAX_1K_MARK_1K_VF1,
      DFS_ALPHA_BETA_1K_MARK_1_VF1,
      DFS_ALPHA_BETA_10K_MARK_1_VF1
//      DFS_NO_ALPHA_BETA_1K_MARK_1_VF1
  ))
}