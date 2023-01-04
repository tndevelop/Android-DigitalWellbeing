package com.example.myapplication.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "path_table")
data class Path(
    @ColumnInfo(name = "intervention") var intervention: Int,
    @ColumnInfo(name = "level") var level: Int,
    @ColumnInfo(name = "app_list") val appList: ArrayList<App>,
    @ColumnInfo(name = "start_date") var start_date: Calendar,
    @ColumnInfo(name = "n_interventions") var n_interventions: Int,
    @ColumnInfo(name = "n_missed_intervention") var n_miss_interventions: Int,
    @ColumnInfo(name = "intervention_duration") var int_duration: Int,
    @ColumnInfo(name = "app_usage_list") val appUsageList: ArrayList<AppUsage>,
    @ColumnInfo(name = "vibration_required") var hasVibration : Boolean,
    @ColumnInfo(name = "display_modification_required") var hasDisplayModification : Boolean,
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long = 0
)

