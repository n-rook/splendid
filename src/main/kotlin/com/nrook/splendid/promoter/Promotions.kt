package com.nrook.splendid.promoter

import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableMap
import com.nrook.splendid.engine.SelfPlayEngine
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.reporting.NullReporter
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.cards.loadComponentsFromFile
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import kotlin.collections.ArrayList

fun oneGame(playerOne: SynchronousAi, playerTwo: SynchronousAi) {
  val random = Random()
  val components = loadComponentsFromFile()
  val game = components.startGame(random)
  val engine = SelfPlayEngine(
      ImmutableMap.of(Player.ONE, playerOne, Player.TWO, playerTwo),
      NullReporter())

  val victor = engine.run(game)

  print("Winner: $victor")
}

fun winRate(playerOne: SynchronousAi, playerTwo: SynchronousAi, games: Int) {
  val random = Random()
  val components = loadComponentsFromFile()
  val reporter = NullReporter()
  val engine = SelfPlayEngine(
      ImmutableMap.of(Player.ONE, playerOne, Player.TWO, playerTwo),
      reporter)

  val forkJoinPool = ForkJoinPool()
  val tasks = ArrayList<ForkJoinTask<Player>>()
  for (i in 1..games) {
    tasks.add(
        forkJoinPool.submit(
            ForkJoinTask.adapt(Callable<Player> { engine.run(components.startGame(random)) })))
  }
  val outcomes = HashMultiset.create<Player>()
  for (task in tasks) {
    outcomes.add(task.join())
  }

  val winrate = outcomes.count(Player.ONE).toDouble() / outcomes.size
  print("Win rate: $winrate (${outcomes.count(Player.ONE)} to ${outcomes.count(Player.TWO)})")
}