package com.example.myapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.db.data.Path

@Dao
interface PathDao {

    @Insert
    suspend fun addPath(path: Path): Long

    @Delete
    suspend fun deletePath(path: Path)

    @Update
    suspend fun updatePath(path: Path)

    @Query("DELETE FROM path_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM path_table")
    suspend fun getPathList(): List<Path>

    @Query("SELECT * FROM path_table")
    fun getPathListV1(): List<Path>

    @Query("SELECT * FROM path_table")
    fun getPathListV2(): LiveData<List<Path>>

    @Query("SELECT * FROM path_table WHERE intervention = :intType")
    suspend fun getPathListIntV1(intType: Int): List<Path>

    @Query("SELECT * FROM path_table")
    fun getPathListIntV2(): LiveData<List<Path>>

    @Query("SELECT * FROM path_table WHERE id = :id")
    suspend fun getPathForIntervention(id:Long): Path
}