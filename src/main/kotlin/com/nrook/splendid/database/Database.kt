package com.nrook.splendid.database

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.io.Resources
import com.nrook.splendid.database.mappers.AiMapper
import com.nrook.splendid.database.mappers.AiRow
import com.nrook.splendid.database.mappers.GameMapper
import com.nrook.splendid.rules.Player
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.sql.Connection

/**
 * Controls access to the database. Use this class for everything.
 */
class Database(private val sqlSessionFactory: SqlSessionFactory) {

  fun createTables() {
    val resource = Resources.getResource("tables/create.sql")
    val sql = Resources.readLines(resource, Charset.forName("UTF-8"))
    val sqlStatements = splitIntoStatements(sql)

    sqlSessionFactory.openSession().use {
      for (statement in sqlStatements) {
        try {
          it.connection.createStatement().execute(statement)
        } catch (e: Exception) {
          throw RuntimeException("Error executing statement\n$statement", e)
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
      return if(identity == null) null else AiIdentity(identity.id, identity.name)
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

  fun recordGame(playerOne: AiIdentity, playerTwo: AiIdentity, winner: Player) {
    sqlSessionFactory.openSession().use {
      it.getMapper(GameMapper::class.java)
          .insertGame(playerOne.id, playerTwo.id, winner.ordinal)
      it.commit()
    }
  }

  fun getGames(aiMap: Map<Int, AiIdentity>): ImmutableList<Game> {
    sqlSessionFactory.openSession().use { session ->
      return ImmutableList.copyOf(
          session.getMapper(GameMapper::class.java).selectAllGames()
          .map { Game.create(it, aiMap) })
    }
  }
}
