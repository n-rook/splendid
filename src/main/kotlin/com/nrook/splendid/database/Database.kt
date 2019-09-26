package com.nrook.splendid.database

import GameMetadata
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.io.Resources
import com.nrook.splendid.auth.UserAccount
import com.nrook.splendid.database.mappers.AiMapper
import com.nrook.splendid.database.mappers.AiRow
import com.nrook.splendid.database.mappers.GameMapper
import com.nrook.splendid.database.mappers.TrainingGameRecordMapper
import com.nrook.splendid.database.mappers.UserAccountMapper
import com.nrook.splendid.rules.Player
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import java.nio.charset.Charset
import java.time.Instant

/**
 * In the database, we store all times as seconds past the epoch.
 */
private fun toDbTime(startTime: Instant) = startTime.epochSecond

private fun fromDbTime(startTime: Long) = Instant.ofEpochSecond(startTime)

/**
 * Controls access to the database. Use this class for everything.
 */
class Database(private val sqlSessionFactory: SqlSessionFactory) {

  /**
   * Create tables using create.sql for the first time.
   */
  fun createTables() {
    runScript("tables/create.sql")
  }

  /**
   * Deletes all mutable data. Does not delete table from create.sql. Useful for tests only.
   */
  fun deleteAllData() {
    runScript("tables/clear.sql")
  }

  private fun runScript(scriptPath: String) {
    val resource = Resources.getResource(scriptPath)
    val sql = Resources.readLines(resource, Charset.forName("UTF-8"))
    val sqlStatements = splitIntoStatements(sql)

    sqlSessionFactory.openSession().use {
      for (statement in sqlStatements) {
        try {
          it.connection.createStatement().execute(statement)
        } catch (e: Exception) {
          throw RuntimeException(
              "Error executing statement\n$statement \nfrom script$scriptPath", e)
        }
      }
    }
  }

  private fun splitIntoStatements(lines: List<String>): ImmutableList<String> {
    val statements = ImmutableList.builder<String>()
    var currentStatement = StringBuilder()
    for (line in lines) {
      currentStatement.append(line)
      currentStatement.appendln()
      if (line.endsWith(";")) {
        statements.add(currentStatement.toString())
        currentStatement = StringBuilder()
      }
    }

    return statements.build()
  }

  fun recordAi(string: String) {
    sqlSessionFactory.openSession().connection
    val sqlSession = sqlSessionFactory.openSession()
    sqlSession.getMapper(AiMapper::class.java).insertAi(string)
    sqlSession.commit()
    sqlSession.close()
  }

  fun getAiByName(name: String): AiIdentity? {
    sqlSessionFactory.openSession().use {
      val identity: AiRow? = it.getMapper(AiMapper::class.java).getAiByName(name)
      return if (identity == null) null else AiIdentity(identity.id, identity.name)
    }
  }

  fun getAis(): ImmutableMap<Int, AiIdentity> {
    sqlSessionFactory.openSession().use { session ->
      val aiList = session.getMapper(AiMapper::class.java).getAis()
      val builder = ImmutableMap.builder<Int, AiIdentity>()
      for (ai in aiList) {
        val identity = AiIdentity(ai.id, ai.name)
        builder.put(identity.id, identity)
      }
      return builder.build()
    }
  }

  /**
   * Record the outcome of a training game.
   */
  fun recordTrainingGameRecord(
      playerOne: AiIdentity, playerTwo: AiIdentity, winner: Player, startTime: Instant) {
    sqlSessionFactory.openSession().use {
      it.getMapper(TrainingGameRecordMapper::class.java)
          .insertGame(playerOne.id, playerTwo.id, winner.ordinal, startTime.epochSecond)
      it.commit()
    }
  }

  fun getTrainingGameRecords(aiMap: Map<Int, AiIdentity>): ImmutableList<TrainingGameRecord> {
    sqlSessionFactory.openSession().use { session ->
      return ImmutableList.copyOf(
          session.getMapper(TrainingGameRecordMapper::class.java).selectAllGames()
              .map { TrainingGameRecord.create(it, aiMap) })
    }
  }

  /**
   * Create a new user account.
   */
  fun createUserAccount(name: String) {
    sqlSessionFactory.openSession().use {
      it.getMapper(UserAccountMapper::class.java).insertUser(name);
      it.commit();
    }
  }

  /**
   * Looks up a new user account (with ID) by name.
   */
  fun lookUpUserAccountByName(name: String): UserAccount? {
    sqlSessionFactory.openSession().use {
      val userDao = it.getMapper(UserAccountMapper::class.java).getUserByName(name)
      return if (userDao != null) UserAccount(userDao.id, userDao.name) else null
    }
  }

  /**
   * Create a new game from scratch.
   *
   * TODO: return whole game info object
   */
  fun createNewGame(
      playerOneUser: UserAccount?,
      playerOneAi: Int?,
      playerTwoUser: UserAccount?,
      playerTwoAi: Int?,
      startTime: Instant): Int {
    sqlSessionFactory.openSession().use {
      val gameId = it.getMapper(GameMapper::class.java)
          .insertGame(playerOneUser?.id, playerOneAi, playerTwoUser?.id, playerTwoAi, toDbTime(startTime))
      it.commit()
      return gameId
    }
  }

  /**
   * Get a GameMetadata by ID.
   */
  fun getGameById(id: Int): GameMetadata {
    sqlSessionFactory.openSession().use {
      val gameDao = it.getMapper(GameMapper::class.java)
          .getById(id)
          ?: throw Error("No game found for ID $id");
      val transactionBuilder = TransactionBuilder(it)
      val metadata = GameMetadata(
          id,
          if (gameDao.playerOneUser == null) null else transactionBuilder.getUserAccountById(gameDao.playerOneUser),
          gameDao.playerOneAi,
          if (gameDao.playerTwoUser == null) null else transactionBuilder.getUserAccountById(gameDao.playerTwoUser),
          gameDao.playerTwoAi,
          fromDbTime(gameDao.startTime)
      )
      it.commit()
      return metadata
    }
  }
}

class TransactionBuilder(private val session: SqlSession) {
  fun createGame() {
//    session.getMapper(GameMapper::class.java)
  }

  fun getUserAccountById(id: Int): UserAccount {
    val userDao = session.getMapper(UserAccountMapper::class.java).getUserById(id)
        ?: throw Error("No user found with ID $id")
    return UserAccount(userDao.id, userDao.name)
  }

  fun commit() {
    session.commit()
  }
}
