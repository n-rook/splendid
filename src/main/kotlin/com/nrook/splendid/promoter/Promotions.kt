package com.nrook.splendid.promoter

import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import com.nrook.splendid.database.AiIdentity
import com.nrook.splendid.database.Database
import com.nrook.splendid.database.createProductionDatabase
import com.nrook.splendid.engine.RandomShuffler
import com.nrook.splendid.engine.SelfPlayEngine
import com.nrook.splendid.engine.SynchronousAi
import com.nrook.splendid.reporting.ConsoleReporter
import com.nrook.splendid.reporting.ConsoleShortReporter
import com.nrook.splendid.reporting.NullReporter
import com.nrook.splendid.reporting.Reporter
import com.nrook.splendid.rules.Player
import com.nrook.splendid.rules.cards.GameComponents
import com.nrook.splendid.rules.cards.loadComponentsFromFile
import java.time.Instant
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import kotlin.collections.ArrayList

private val CUTOFF = 75

fun oneGame(playerOne: SynchronousAi, playerTwo: SynchronousAi) {
  val random = Random()
  val components = loadComponentsFromFile()
  val game = components.startGame(random)
  val engine = SelfPlayEngine(
      ImmutableMap.of(Player.ONE, playerOne, Player.TWO, playerTwo),
      RandomShuffler(random),
      ConsoleReporter(),
      CUTOFF)

  val victor = engine.run(game)

  print("Winner: $victor")
}

fun winRate(playerOne: SynchronousAi, playerTwo: SynchronousAi, games: Int) {
  val random = Random()
  val components = loadComponentsFromFile()
  val reporter = ConsoleShortReporter()
  val engine = SelfPlayEngine(
      ImmutableMap.of(Player.ONE, playerOne, Player.TWO, playerTwo),
      RandomShuffler(random),
      reporter,
      CUTOFF)

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

private const val SELF_PLAY_FOREVER_PARALLEL_GAMES = 4

fun selfPlayForever(players: ImmutableList<NamedPlayer>) {
  val components = loadComponentsFromFile()
  val reporter = ConsoleShortReporter()
  val db = createProductionDatabase()
  val dbPlayers = setUpPlayers(db, players)
  val nameToPlayer: ImmutableMap<String, AiIdentity> =
      Maps.uniqueIndex(dbPlayers.values) { it!!.name }

  printEloFromDatabase(db)

  for (i in 1..SELF_PLAY_FOREVER_PARALLEL_GAMES) {
    val t = Thread {
      runParallelGames(players, reporter, components, db, nameToPlayer)
    }
    t.start()
  }

//  runParallelGames(players, reporter, components, db, nameToPlayer)
}

private fun runParallelGames(
    players: ImmutableList<NamedPlayer>,
    reporter: Reporter,
    components: GameComponents,
    db: Database,
    nameToPlayer: ImmutableMap<String, AiIdentity>) {
  val random = Random()
  while (true) {
    val (playerOne, playerTwo) = pickTwo(random, players)
    print("${playerOne.name} VS ${playerTwo.name}")
    val startTime = Instant.now()
    val engine = SelfPlayEngine(
        ImmutableMap.of(Player.ONE, playerOne.ai, Player.TWO, playerTwo.ai),
        RandomShuffler(random),
        reporter,
        CUTOFF)
    val victor = engine.run(components.startGame(random))
    println("The winner is ${if (victor == Player.ONE) playerOne.name else playerTwo.name}")
    db.recordGame(
        nameToPlayer[playerOne.name]!!, nameToPlayer[playerTwo.name]!!, victor, startTime)

    printEloFromDatabase(db)
  }
}

private fun setUpPlayers(db: Database, players: ImmutableList<NamedPlayer>): ImmutableMap<Int, AiIdentity> {
  for (player in players) {
    if (db.getAiByName(player.name) == null) {
      db.recordAi(player.name)
    }
  }
  return db.getAis()
}

private fun <E> pickTwo(random: Random, list: List<E>): Pair<E, E> {
  if (list.size < 2) {
    throw RuntimeException("List too short")
  }

  val firstIndex = random.nextInt(list.size)
  val secondIndex = run {
    var candidate: Int
    do {
      candidate = random.nextInt(list.size)
    } while (candidate == firstIndex)

    candidate
  }

  return Pair(list[firstIndex], list[secondIndex])
}

data class NamedPlayer(val ai: SynchronousAi, val name: String)
