package com.nrook.splendid.promoter

import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableMap
import com.nrook.splendid.engine.SelfPlayEngine
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.reporting.ConsoleReporter
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.cards.loadComponentsFromFile
import java.util.*

fun oneGame(playerOne: SynchronousAi, playerTwo: SynchronousAi) {
  val random = Random()
  val components = loadComponentsFromFile()
  val game = components.startGame(random)
  val engine = SelfPlayEngine(
      ImmutableMap.of(Player.ONE, playerOne, Player.TWO, playerTwo),
      ConsoleReporter())

  val victor = engine.run(game)

  print("Winner: $victor")
}

fun winRate(playerOne: SynchronousAi, playerTwo: SynchronousAi, games: Int) {
  val random = Random()
  val components = loadComponentsFromFile()
  val engine = SelfPlayEngine(
      ImmutableMap.of(Player.ONE, playerOne, Player.TWO, playerTwo),
      ConsoleReporter())

  val outcomes = HashMultiset.create<Player>()
  for (i in 1..games) {
    outcomes.add(engine.run(components.startGame(random)))
  }

  val winrate = outcomes.count(Player.ONE) / outcomes.size
  print("Win rate: $winrate (${outcomes.count(Player.ONE)} to ${outcomes.count(Player.TWO)})")
}