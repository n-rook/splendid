package com.nrook.splendid.database

import com.nrook.splendid.database.mappers.TrainingGameRecordDao
import com.nrook.splendid.rules.Player
import java.time.Instant

data class TrainingGameRecord(val playerOne: AiIdentity, val playerTwo: AiIdentity, val outcome: Player, val time: Instant) {
  companion object {
    fun create(game: TrainingGameRecordDao, aiMap: Map<Int, AiIdentity>): TrainingGameRecord {
      return TrainingGameRecord(
          aiMap[game.playerOne] ?: throw Error("No known AI ${game.playerOne}"),
          aiMap[game.playerTwo] ?: throw Error("No known AI ${game.playerTwo}"),
          Player.values()[game.outcome],
          Instant.ofEpochSecond(game.startTime)
      )
    }
  }
}
