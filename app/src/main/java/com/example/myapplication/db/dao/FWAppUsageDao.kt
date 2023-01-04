package com.example.myapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.db.data.FWAppUsage

@Dao
interface FWAppUsageDao {
    @Insert
    suspend fun addFWUsage(usage: FWAppUsage)

    @Delete
    suspend fun deleteFWUsage(usage: FWAppUsage)

    @Update
    suspend fun updateFWUsage(usage: FWAppUsage)

    @Query("SELECT * FROM first_week_app_usage WHERE path_id = :id")
    fun getUsage(id: Long): LiveData<FWAppUsage>
}