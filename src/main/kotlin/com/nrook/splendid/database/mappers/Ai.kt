package com.nrook.splendid.database.mappers

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Select

interface AiMapper {

  @Insert("INSERT INTO Ai (name) VALUES (#{name})")
  fun insertAi(name: String)

  @Select("SELECT id, name FROM Ai WHERE name = #{name}")
  fun getAiByName(name: String): AiRow

  @Select("SELECT id, name FROM Ai")
  fun getAis(): List<AiRow>
}

data class AiRow(val id: Int, val name: String) {
  constructor(id: Any, name: Any): this(id as Int, name as String)
}
