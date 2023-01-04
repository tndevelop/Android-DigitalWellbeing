package com.example.myapplication.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage_table")
data class AppUsage(
    @ColumnInfo(name = "avg_usage") val avgUsage: Int,
    @PrimaryKey @ColumnInfo(name = "app_package") val appPackage: String
)
