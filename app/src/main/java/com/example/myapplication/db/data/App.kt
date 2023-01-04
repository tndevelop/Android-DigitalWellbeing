package com.example.myapplication.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "excluded_app_table")
data class App(
        @ColumnInfo(name = "app_name") val appName: String,
        @PrimaryKey @ColumnInfo(name = "app_package") val appPackage: String
)
