package com.example.myapplication.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "inactivity_notifications_table",
        foreignKeys = [androidx.room.ForeignKey(
            entity = Path::class,
            parentColumns = ["id"],
            childColumns = ["path_id"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )],
        indices = [Index(value = ["path_id"],
                unique = false)]
        )

data class InactivityNotification(
        @ColumnInfo(name = "path_id") val pathId: Long,
        @ColumnInfo(name = "date") val date: Calendar,
        @ColumnInfo(name = "opened") var opened: Boolean = false,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0
)
