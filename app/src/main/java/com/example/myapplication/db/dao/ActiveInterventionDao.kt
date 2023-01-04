package com.example.myapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.db.data.ActiveIntervention

@Dao
interface ActiveInterventionDao {

    @Insert
    suspend fun addActiveIntervention(int: ActiveIntervention)

    @Query("DELETE FROM active_interventions_table")
    suspend fun removeAllActiveIntervention()

    @Query("DELETE FROM active_interventions_table WHERE id = :int")
    suspend fun removeActiveIntervention(int: Long)

    @Update
    suspend fun updateActiveIntervention(int: ActiveIntervention)

    @Query("SELECT * FROM active_interventions_table")
    fun getActiveInterventions(): LiveData<List<ActiveIntervention>>
}