package com.nrook.splendid.database

import com.nrook.splendid.database.mappers.AiMapper
import com.nrook.splendid.database.mappers.GameMapper
import com.nrook.splendid.database.mappers.TrainingGameRecordMapper
import com.nrook.splendid.database.mappers.UserAccountMapper
import com.nrook.splendid.rules.Player
import mu.KLogging
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.sqlite.SQLiteDataSource
import java.nio.file.Path
import java.time.Instant

val logger = KLogging().logger("DatabaseConnection")

fun main(args: Array<String>) {
  val database = Database(createSqlSessionFactory(getProductionDataSource()))

  database.createTables()
  if (database.getAiByName("first") == null) {
    database.recordAi("first")
  }
  if (database.getAiByName("second") == null) {
    database.recordAi("second")
  }
  val ais = database.getAis()
  val firstAi = ais.values.first { it.name == "first" }
  val secondAi = ais.values.first { it.name == "second" }

  database.recordTrainingGameRecord(firstAi, secondAi, Player.ONE, Instant.now())

  println(database.getTrainingGameRecords(ais))
}

fun createProductionDatabase(): Database {
  val database = Database(createSqlSessionFactory(getProductionDataSource()))
  database.createTables()
  return database
}

fun createSqlSessionFactory(dataSource: SQLiteDataSource): SqlSessionFactory {
  logger.info { "Connecting to database ${dataSource.url} "}
  val transactionFactory = JdbcTransactionFactory()
  val environment = Environment("hello", transactionFactory, dataSource)
  val configuration = Configuration(environment)
  configuration.addMapper(AiMapper::class.java)
  configuration.addMapper(GameMapper::class.java)
  configuration.addMapper(TrainingGameRecordMapper::class.java)
  configuration.addMapper(UserAccountMapper::class.java)

  return SqlSessionFactoryBuilder().build(configuration)!!
}

fun getFileDataSource(file: Path): SQLiteDataSource {
  val stringPath = file.toString()
  val sqliteDataSource = SQLiteDataSource()
  sqliteDataSource.url = "jdbc:sqlite:$stringPath"
  return sqliteDataSource
}

fun getProductionDataSource(): SQLiteDataSource {
  val sqliteDataSource = SQLiteDataSource()
  sqliteDataSource.url = "jdbc:sqlite:local.db"
  return sqliteDataSource
}
