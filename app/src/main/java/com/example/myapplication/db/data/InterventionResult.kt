package com.example.myapplication.db.data

import androidx.room.*
import java.util.*

@Entity(tableName = "result_table",
        foreignKeys = [ForeignKey(
                entity = Path::class,
                parentColumns = ["id"],
                childColumns = ["pathId"],
                onDelete = ForeignKey.CASCADE
        )],
        indices = [Index(value = ["pathId"],
                unique = false)]
)
data class InterventionResult(
        @ColumnInfo(name = "level") val level: Int,
        @ColumnInfo(name = "date") val date: Calendar,
        @ColumnInfo(name = "app_name") val apps: String,
        @ColumnInfo(name = "pathId") val path_id: Long,
        @ColumnInfo(name = "int_type") val type: Int,
        @ColumnInfo(name = "int2_start_penalty") val start_pen: Boolean = false,
        @ColumnInfo(name = "int2_end_penalty") val end_pen: Boolean = false,
        @ColumnInfo(name = "int2_delay_penalty") val del_pen: Boolean = false,
        @ColumnInfo(name = "int2_seconds_delay") val sec_delay: Int = 0,
        @ColumnInfo(name = "int1_n_unlock") val n_openings: Int = 0,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0
)

