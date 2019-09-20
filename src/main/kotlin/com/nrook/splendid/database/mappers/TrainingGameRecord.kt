package com.nrook.splendid.database.mappers

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

interface TrainingGameRecordMapper {
  /**
   * Add a new game
   *
   * @param playerOne ID of player 1 in AI
   * @param playerTwo ID of player 2 in AI
   * @param winner 0 if player 1 won, 1 if player 2 won.
   */
  @Insert("INSERT INTO TrainingGameRecord (playerOne, playerTwo, outcome, startTime) VALUES (#{p1}, #{p2}, #{winner}, #{startTime})")
  fun insertGame(
      @Param("p1") p1: Int,
      @Param("p2") p2: Int,
      @Param("winner") winner: Int,
      @Param("startTime") startTime: Long)

  /**
   * Select all games.
   */
  @Select("SELECT playerOne, playerTwo, outcome, startTime FROM TrainingGameRecord")
  fun selectAllGames(): List<TrainingGameRecordDao>
}

data class TrainingGameRecordDao(val playerOne: Int, val playerTwo: Int, val outcome: Int, val startTime: Long) {
  constructor(playerOne: Any, playerTwo: Any, outcome: Any, startTime: Any):
      this(playerOne as Int, playerTwo as Int, outcome as Int, (startTime as Int).toLong())
}
