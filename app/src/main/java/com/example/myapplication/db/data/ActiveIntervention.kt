package com.example.myapplication.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_interventions_table",
        foreignKeys = [androidx.room.ForeignKey(
                entity = Path::class,
                parentColumns = ["id"],
                childColumns = ["id"],
                onDelete = androidx.room.ForeignKey.CASCADE
        )])

data class ActiveIntervention (
        @PrimaryKey @ColumnInfo(name = "id")val intervention_id: Long,
        @ColumnInfo(name = "type") val int_type: Int,
        @ColumnInfo(name = "paused") val paused: Boolean = false
        )
