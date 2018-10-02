package com.nrook.splendid.promoter

import com.google.common.collect.ImmutableMap
import com.nrook.splendid.ai.DirectValuePlayer
import com.nrook.splendid.ai.GeneralFeature
import com.nrook.splendid.ai.PlayerSpecificFeature
import com.nrook.splendid.ai.RandomPlayer
import com.nrook.splendid.ai.TopLevelShuffingPlayer
import com.nrook.splendid.ai.ValueFunction
import com.nrook.splendid.ai.depthfirst.DepthFirstMinimaxPlayer
import com.nrook.splendid.ai.minimax.MinimaxPlayer
import java.util.*

val random = Random()

val RANDOM_PLAYER = NamedPlayer(RandomPlayer(random), "Random Player")

private val VALUE_FUNCTION_ONE = ValueFunction(
    ImmutableMap.of(
        PlayerSpecificFeature.VICTORY_POINTS, 100.0,
        PlayerSpecificFeature.DEVELOPMENTS, 10.0,  // won vs 100
        PlayerSpecificFeature.CHIPS, 1.0,
        GeneralFeature.SECOND_PLAYER_EXTRA_TURN_BONUS, 5.0
    )
)

// A strange and frightening fact:
// DIRECT_VF1 appears to be stronger than MINIMAX_1K_MARK_1_VF1,
// which in turn is stronger than MINIMAX_10K_MARK_1_VF1.
val DIRECT_VF1 = NamedPlayer(DirectValuePlayer(random, VALUE_FUNCTION_ONE), "Direct VF1")

val MINIMAX_10K_MARK_1_VF1 = NamedPlayer(
    MinimaxPlayer(VALUE_FUNCTION_ONE, 10000), "Minimax 10K Mark 1 VF1")

val MINIMAX_1K_MARK_1_VF1 = NamedPlayer(
    MinimaxPlayer(VALUE_FUNCTION_ONE, 1000), "Minimax 1K Mark 1 VF1")

val X10_SHUFFLER_MINIMAX_1K_MARK_1K_VF1 = NamedPlayer(
    TopLevelShuffingPlayer.createFromCopies(random, 10) {
      MINIMAX_1K_MARK_1_VF1.ai
    },
    "10X Shuffler ${MINIMAX_1K_MARK_1_VF1.name}"
)

//val DFS_NO_ALPHA_BETA_1K_MARK_1_VF1 = NamedPlayer(
//    DepthFirstMinimaxPlayer(VALUE_FUNCTION_ONE, 1000), "DFS No Alpha Beta 1K Mark 1 VF1")

val DFS_ALPHA_BETA_1K_MARK_1_VF1 = NamedPlayer(
    DepthFirstMinimaxPlayer(VALUE_FUNCTION_ONE, 1000), "DFS Alpha Beta 1K Mark 1 VF1")

val DFS_ALPHA_BETA_10K_MARK_1_VF1 = NamedPlayer(
    DepthFirstMinimaxPlayer(VALUE_FUNCTION_ONE, 10000), "DFS Alpha Beta 10K Mark 1 VF1")
