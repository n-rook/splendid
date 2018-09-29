package com.nrook.splendid.database.mappers

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

interface GameMapper {
  /**
   * Add a new game
   *
   * @param playerOne ID of player 1 in AI
   * @param playerTwo ID of player 2 in AI
   * @param winner 0 if player 1 won, 1 if player 2 won.
   */
  @Insert("INSERT INTO Games (playerOne, playerTwo, outcome) VALUES (#{p1}, #{p2}, #{winner})")
  fun insertGame(
      @Param("p1") p1: Int,
      @Param("p2") p2: Int,
      @Param("winner") winner: Int)

  /**
   * Select all games.
   */
  @Select("SELECT playerOne, playerTwo, outcome FROM Games")
  fun selectAllGames(): List<GameDao>
}

data class GameDao(val playerOne: Int, val playerTwo: Int, val outcome: Int) {
  constructor(playerOne: Any, playerTwo: Any, outcome: Any):
      this(playerOne as Int, playerTwo as Int, outcome as Int)
}
