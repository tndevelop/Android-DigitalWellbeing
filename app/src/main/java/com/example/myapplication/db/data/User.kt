package com.example.myapplication.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "user_table")
data class User(
        @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
        @ColumnInfo(name = "monitoring_start_date") val startDate: Calendar,
)
