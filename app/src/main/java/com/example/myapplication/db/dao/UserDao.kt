package com.example.myapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.db.data.User

@Dao
interface UserDao {
    @Insert
    suspend fun setUser(user: User)

    @Query("DELETE FROM user_table")
    suspend fun deleteUser()

    @Query("SELECT * FROM user_table LIMIT 1")
    suspend fun getUser(): User

    @Query("SELECT * FROM user_table LIMIT 1")
    fun getUserV2(): LiveData<User>

    @Query("SELECT * FROM user_table")
    suspend fun checkUser(): User?
}