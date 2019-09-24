package com.nrook.splendid.database.testing

import com.nrook.splendid.database.Database
import com.nrook.splendid.database.createSqlSessionFactory
import com.nrook.splendid.database.getFileDataSource
import java.nio.file.Files
import java.nio.file.Path

/**
 * Holds a test database.
 */
class TestDatabase(private val database: Database, private val path: Path) {
  fun getCleanDatabase(): Database {
    database.deleteAllData()
    return database
  }
}

private fun createTestDatabase(): TestDatabase {
  val tempDirectory = Files.createTempDirectory("database")
  val databaseFile = tempDirectory.resolve("temp.db")

  val dataSource = getFileDataSource(databaseFile)
  val database = Database(createSqlSessionFactory(dataSource))
  database.createTables()

  return TestDatabase(database, databaseFile);
}

private val testDatabase: TestDatabase by lazy { createTestDatabase() }

/**
 * Returns a clean test database.
 *
 * If this is the first time this method has been called, we will create a new test database
 * from a temp file. If not, we will delete most data from the existing test database and return
 * it.
 */
fun createTestDb(): Database {
  return testDatabase.getCleanDatabase()
}
