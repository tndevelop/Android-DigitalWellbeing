package com.example.myapplication.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.db.data.NotificationDate

@Dao
interface NotificationDateDao {
    @Query("DELETE FROM notification_date_table")
    suspend fun deleteDate()

    @Insert
    suspend fun setDate(date: NotificationDate)

    @Query("SELECT * FROM notification_date_table LIMIT 1")
    suspend fun getDate(): NotificationDate?
}