package com.nrook.splendid.rating

import com.google.common.collect.ComparisonChain
import com.google.common.collect.ImmutableMap
import com.nrook.splendid.database.AiIdentity
import com.nrook.splendid.rules.Player
import kotlin.math.pow

private const val INITIAL_RATING = 1000.0
private const val ELO_K = 20  // Maximum rating change per game
private const val EXPECTED_WIN_RATE_CONSTANT = 400

class Ratings(private val map: MutableMap<AiIdentity, Record>) {

  constructor(): this(HashMap())

  @Synchronized
  fun recordGame(playerOne: AiIdentity, playerTwo: AiIdentity, winner: Player) {
    when (winner) {
      Player.ONE -> recordGame(playerOne, playerTwo)
      Player.TWO -> recordGame(playerTwo, playerOne)
    }
  }

  private fun recordGame(winner: AiIdentity, loser: AiIdentity) {
    initialize(winner)
    initialize(loser)

    val delta = ELO_K * (1.0 - expectedWinRate(map[winner]!!.elo, map[loser]!!.elo))

    map[winner]!!.recordGame(delta)
    map[loser]!!.recordGame(-delta)
  }

  private fun initialize(player: AiIdentity) {
    if (!map.containsKey(player)) {
      map[player] = Record(0, INITIAL_RATING)
    }
  }

  @Synchronized
  fun getRatingsMap(): ImmutableMap<AiIdentity, Record> {
    return ImmutableMap.copyOf(map)
  }
}

class Record(private var privateNumGames: Int, private var privateElo: Double):
    Comparable<Record> {

  val numGames: Int get() = privateNumGames
  val elo: Double get() = privateElo

  fun recordGame(delta: Double) {
    privateNumGames++
    privateElo += delta
  }

  override fun compareTo(other: Record): Int {
    return ComparisonChain.start()
        .compare(this.elo, other.elo)
        .compare(this.numGames, other.numGames)
        .result()
  }
}

fun expectedWinRate(rating1: Double, rating2: Double): Double {
  return 1.0 / (1.0 + 10.0.pow((rating2 - rating1) / EXPECTED_WIN_RATE_CONSTANT))
}
