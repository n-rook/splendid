package com.nrook.splendid.promoter

import com.google.common.collect.ImmutableMap
import com.nrook.splendid.ai.RandomPlayer
import com.nrook.splendid.engine.SelfPlayEngine
import com.nrook.splendid.reporting.ConsoleReporter
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.cards.loadComponentsFromFile
import java.util.*

private fun selfPlayRandom() {
  val random = Random()
  val playerOneAi = RandomPlayer(random)
  val playerTwoAi = RandomPlayer(random)

  val components = loadComponentsFromFile()
  val game = components.startGame(random)
  val engine = SelfPlayEngine(
      ImmutableMap.of(Player.ONE, playerOneAi, Player.TWO, playerTwoAi),
      ConsoleReporter())

  val victor = engine.run(game)

  print("Winner: $victor")
}

fun main(args: Array<String>) {
  selfPlayRandom()
}
