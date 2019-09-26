package com.nrook.splendid.database

import com.google.common.truth.Truth
import com.nrook.splendid.database.testing.createTestDb
import com.nrook.splendid.rules.Player
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

val SOME_INSTANT = LocalDateTime.of(2018, 1, 1, 12, 0).toInstant(ZoneOffset.UTC)

class DatabaseTest {

  lateinit var database: Database

  @Before
  fun createTestDatabase() {
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

    database.recordTrainingGameRecord(first, second, Player.ONE, gameTime)

    val games = database.getTrainingGameRecords(ais)
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

  @Test
  fun createNewGame() {
    database.createUserAccount("Some User!")
    val account = database.lookUpUserAccountByName("Some User!")!!;
    database.recordAi("Some Ai!");
    val ai = database.getAiByName("Some Ai!")!!


    val id = database.createNewGame(account, null, null, ai.id, SOME_INSTANT)

    val game = database.getGameById(id);

    Truth.assertThat(game.id).isEqualTo(id);
    Truth.assertThat(game.playerOneUser?.name).isEqualTo("Some User!")
    Truth.assertThat(game.playerOneAi).isNull()
    Truth.assertThat(game.playerTwoUser).isNull()
    Truth.assertThat(game.playerTwoAi).isEqualTo(ai.id)
    Truth.assertThat(game.startTime).isEqualTo(SOME_INSTANT)
  }
}
