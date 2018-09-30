package com.nrook.splendid.database

import com.nrook.splendid.database.mappers.GameDao
import com.nrook.splendid.rules.Player
import java.time.Instant

data class Game(val playerOne: AiIdentity, val playerTwo: AiIdentity, val outcome: Player, val time: Instant) {
  companion object {
    fun create(game: GameDao, aiMap: Map<Int, AiIdentity>): com.nrook.splendid.database.Game {
      return Game(
          aiMap[game.playerOne] ?: throw Error("No known AI ${game.playerOne}"),
          aiMap[game.playerTwo] ?: throw Error("No known AI ${game.playerTwo}"),
          Player.values()[game.outcome],
          Instant.ofEpochSecond(game.startTime)
      )
    }
  }
}
