package com.example.myapplication.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_date_table")
data class NotificationDate(
        @PrimaryKey @ColumnInfo(name = "date") val date: String
)
