package com.nrook.splendid.database.mappers

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

interface GameMapper {
  @Insert("INSERT INTO Game (playerOneUser, playerOneAi, playerTwoUser, playerTwoAi, startTime) " +
      "VALUES (#{p1U}, #{p1A}, #{p2U}, #{p2A}, #{startTime})")
  @Options(useGeneratedKeys = true, keyColumn = "id")
  fun insertGame(
      @Param("p1U") playerOneUser: Int?,
      @Param("p1A") playerOneAi: Int?,
      @Param("p2U") playerTwoUser: Int?,
      @Param("p2A") playerTwoAi: Int?,
      @Param("startTime") startTime: Long): Int

  @Select("SELECT id, playerOneUser, playerOneAi, playerTwoUser, playerTwoAi, startTime FROM Game " +
      "WHERE id = #{id}")
  fun getById(@Param("id") id: Int): GameDao?
}

data class GameDao(val id: Int,
                   val playerOneUser: Int?,
                   val playerOneAi: Int?,
                   val playerTwoUser: Int?,
                   val playerTwoAi: Int?,
                   val startTime: Long) {
  constructor(id: Any, playerOneUser: Any?, playerOneAi: Any?, playerTwoUser: Any?, playerTwoAi: Any?, startTime: Any) :
      this(
          id as Int,
          playerOneUser as Int?,
          playerOneAi as Int?,
          playerTwoUser as Int?,
          playerTwoAi as Int?,
          (startTime as Int).toLong()
      );
}

//interface AiMapper {
//
//  @Insert("INSERT INTO Ai (name) VALUES (#{name})")
//  fun insertAi(name: String)
//
//  @Select("SELECT id, name FROM Ai WHERE name = #{name}")
//  fun getAiByName(name: String): AiRow
//
//  @Select("SELECT id, name FROM Ai")
//  fun getAis(): List<AiRow>
//}
//
//data class AiRow(val id: Int, val name: String) {
//  constructor(id: Any, name: Any): this(id as Int, name as String)
//}
