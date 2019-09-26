package com.nrook.splendid.database.mappers

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Select

interface UserAccountMapper {

  @Insert("INSERT INTO UserAccount (name) VALUES (#{name})")
  fun insertUser(name: String)

  @Select("SELECT id, name FROM UserAccount WHERE name = #{name}")
  fun getUserByName(name: String): UserAccountDao?

  @Select("SELECT id, name FROM UserAccount WHERE id = #{id}")
  fun getUserById(id: Int): UserAccountDao?
}

/**
 * A user account data object.
 */
data class UserAccountDao(val id: Int, val name: String) {
  constructor(id: Any, name: Any): this(id as Int, name as String)
}
