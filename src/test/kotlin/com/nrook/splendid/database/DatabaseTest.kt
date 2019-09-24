package com.nrook.splendid.database

import com.google.common.truth.Truth
import com.nrook.splendid.database.testing.createTestDb
import com.nrook.splendid.rules.Player
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class DatabaseTest {

  lateinit var database: Database

  @Before
  fun before() {
    database = createTestDb()
  }

  @After
  fun deleteTempDirectory() {
//    Figure out how to close the db connection
//    MoreFiles.deleteRecursively(tempDirectory, RecursiveDeleteOption.ALLOW_INSECURE)
  }

  @Test
  fun getAi() {
    database.recordAi("first")
    database.recordAi("second")

    val first = database.getAiByName("first")
    Truth.assertThat(first).isNotNull()
    Truth.assertThat(first!!.name).isEqualTo("first")
  }

  @Test
  fun recordGame() {
    database.recordAi("first")
    database.recordAi("second")
    val ais = database.getAis()
    val first = ais.values.first { it.name == "first" }
    val second = ais.values.first { it.name == "second"}

    val gameTime = LocalDateTime.of(2018, 1, 1, 12, 0).toInstant(ZoneOffset.UTC)

    database.recordGame(first, second, Player.ONE, gameTime)

    val games = database.getGames(ais)
    Truth.assertThat(games).hasSize(1)
    val game = games[0]!!
    Truth.assertThat(game.playerOne).isEqualTo(first)
    Truth.assertThat(game.playerTwo).isEqualTo(second)
    Truth.assertThat(game.outcome).isEqualTo(Player.ONE)
    Truth.assertThat(game.time).isEqualTo(gameTime)
  }

  @Test
  fun deleteAllDataDeletesAi() {
    database.recordAi("first")
    Truth.assertThat(database.getAiByName("first")).isNotNull()

    database.deleteAllData()
    Truth.assertThat(database.getAiByName("first")).isNull()
  }
}
