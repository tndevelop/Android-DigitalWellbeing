package com.example.myapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.db.data.App


@Dao
interface AppDao {

    @Insert
    suspend fun addExcludedApp(app: App)

    @Delete
    suspend fun deleteExcludedApp(app: App)

    @Query("SELECT * FROM excluded_app_table")
    suspend fun getExcludedAppList(): List<App>

    @Query("SELECT * FROM excluded_app_table WHERE app_package = :appPackage")
    fun getExcludedApp(appPackage: String): LiveData<App>

}
