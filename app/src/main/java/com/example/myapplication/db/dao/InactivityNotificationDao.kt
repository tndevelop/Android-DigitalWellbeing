package com.example.myapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.db.data.InactivityNotification

@Dao
interface InactivityNotificationDao {

    @Insert
    suspend fun addNotification(not: InactivityNotification)

    @Delete
    suspend fun removeNotification(not: InactivityNotification)

    @Update
    suspend fun markOpenedNotification(not:InactivityNotification)

    @Query("SELECT * FROM inactivity_notifications_table WHERE opened = 0")
    fun getNotificationV1(): LiveData<List<InactivityNotification>>

    @Query("SELECT * FROM inactivity_notifications_table")
    suspend fun getNotificationV2(): List<InactivityNotification>
}