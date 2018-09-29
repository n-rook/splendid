package com.nrook.splendid.database

import java.sql.Connection
import java.sql.DriverManager

fun main(args: Array<String>) {
  val connection: Connection = DriverManager.getConnection("jdbc:sqlite:test.db")

  val database = Database(connection)

  database.createTables()
}

