package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

  @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
  suspend fun findByMobile(mobile: String): UserEntity?

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1") suspend fun findById(id: String): UserEntity?

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  fun observeById(id: String): Flow<UserEntity?>

  /**
   * ABORT rather than REPLACE: two accounts must never share a mobile number, and silently
   * overwriting an existing account would destroy someone's data. The resulting constraint
   * violation is what makes registration race-safe.
   */
  @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(user: UserEntity)

  @Update suspend fun update(user: UserEntity)
}
