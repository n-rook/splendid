package com.nrook.splendid.database

import com.nrook.splendid.database.mappers.AiMapper
import com.nrook.splendid.database.mappers.GameMapper
import com.nrook.splendid.rules.Player
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.sqlite.SQLiteDataSource

fun main(args: Array<String>) {
//  val connection: Connection = DriverManager.getConnection("jdbc:sqlite:test.db")

  val database = Database(createSqlSessionFactory())

  database.createTables()

//  database.recordAi("robocop")

//  val aiByName = database.getAiByName("lobocop")
//  println(aiByName)

  if (database.getAiByName("first") == null) {
    database.recordAi("first")
  }
  if (database.getAiByName("second") == null) {
    database.recordAi("second")
  }
  val ais = database.getAis()
  val firstAi = ais.values.first { it.name == "first" }
  val secondAi = ais.values.first { it.name == "second" }

  database.recordGame(firstAi, secondAi, Player.ONE)

  println(database.getGames(ais))
}

fun createSqlSessionFactory(): SqlSessionFactory {
  val transactionFactory = JdbcTransactionFactory()
  val environment = Environment("hello", transactionFactory, getDataSource())
  val configuration = Configuration(environment)
  configuration.addMapper(AiMapper::class.java)
  configuration.addMapper(GameMapper::class.java)

  return SqlSessionFactoryBuilder().build(configuration)!!
}

fun getDataSource(): SQLiteDataSource {
//  val sqLiteConfig = SQLiteConfig()
  val sqliteDataSource = SQLiteDataSource()
  sqliteDataSource.url = "jdbc:sqlite:test.db"
  return sqliteDataSource
}
