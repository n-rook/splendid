package com.nrook.splendid.promoter

import com.google.common.collect.ImmutableMap
import com.nrook.splendid.database.AiIdentity
import com.nrook.splendid.database.Database
import com.nrook.splendid.rating.Ratings
import com.nrook.splendid.rating.Record

fun computeEloFromDatabase(db: Database): ImmutableMap<AiIdentity, Record> {
  val ais = db.getAis()
  val games = db.getTrainingGameRecords(ais)
  val ratings = Ratings()

  for (game in games) {
    ratings.recordGame(game.playerOne, game.playerTwo, game.outcome)
  }

  return ratings.getRatingsMap()
}

fun printEloFromDatabase(db: Database) {
  val ratings = computeEloFromDatabase(db)
  println("Ratings:")
  val sortedRatings = ratings.entries.sortedBy { it.value }
  for (rating in sortedRatings) {
    println("  ${rating.value.elo}  | ${rating.key} (${rating.value.numGames})")
  }
}
