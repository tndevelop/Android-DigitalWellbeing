package com.example.myapplication.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "usage_notifications_table")
data class BadUsageNotification (
        @PrimaryKey @ColumnInfo(name = "app_package") val appPackage: String,
        @ColumnInfo(name = "app_name") val appName: String,
        @ColumnInfo(name = "date") val date: Calendar,
        @ColumnInfo(name = "time_spent") val timeSpent: Long,
        @ColumnInfo(name = "n_access") val nAccess: Int,
        @ColumnInfo(name = "opened") var opened: Boolean
        )