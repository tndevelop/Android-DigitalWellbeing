package com.example.myapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.db.data.BadUsageNotification

@Dao
interface BadUsageNotificationDao {

    @Insert
    suspend fun addUsage(badUsageNotification: BadUsageNotification): Long

    @Delete
    suspend fun deleteUsage(badUsageNotification: BadUsageNotification)

    @Update
    suspend fun updateUsage(badUsageNotification: BadUsageNotification)

    @Query("SELECT * FROM usage_notifications_table WHERE opened = 0")
    fun getAllUsages(): LiveData<List<BadUsageNotification>>

    @Query("SELECT * FROM usage_notifications_table")
    suspend fun getAllUsages2(): List<BadUsageNotification>
}