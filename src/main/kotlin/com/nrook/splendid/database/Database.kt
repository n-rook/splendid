package com.nrook.splendid.database

import com.google.common.collect.ImmutableList
import com.google.common.io.Resources
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.sql.Connection

class Database(private val connection: Connection) {

  fun createTables() {
    val resource = Resources.getResource("tables/create.sql")
    val sql = Resources.readLines(resource, Charset.forName("UTF-8"))
    val sqlStatements = splitIntoStatements(sql)
    for (statement in sqlStatements) {
      try {
        connection.createStatement().execute(statement)
      } catch (e: Exception) {
        throw RuntimeException("Error executing statement\n$statement", e)
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
}
