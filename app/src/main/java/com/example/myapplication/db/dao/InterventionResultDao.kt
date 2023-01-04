package com.example.myapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.db.data.InterventionResult

@Dao
interface InterventionResultDao {

    @Insert
    suspend fun addResult(result: InterventionResult): Long

    //prendi tutti i risultati di ogni intervento mai fatto
    @Query("SELECT * FROM RESULT_TABLE")
    fun getAllResults() :LiveData<List<InterventionResult>>

    //prendi i risultati di un determinato path per calcolare se avanzare o meno
    @Query("SELECT * FROM RESULT_TABLE WHERE pathId = :int_id")
    fun getPathResults(int_id: Long) :LiveData<List<InterventionResult>>

    @Query("SELECT * FROM RESULT_TABLE WHERE pathId = :int_id")
    fun getPathResultsV2(int_id: Long) :List<InterventionResult>

    //elimino gli interventi appena avanzo di livello
    @Query("DELETE FROM RESULT_TABLE WHERE pathId = :int_id")
    suspend fun deletePathResult(int_id: Long)
}