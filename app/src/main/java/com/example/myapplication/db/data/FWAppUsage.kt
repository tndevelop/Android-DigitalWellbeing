package com.example.myapplication.db.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "first_week_app_usage",
        foreignKeys = [ForeignKey(
                entity = Path::class,
                parentColumns = ["id"],
                childColumns = ["path_id"],
                onDelete = ForeignKey.CASCADE
        )],)
 data class FWAppUsage (
    @PrimaryKey @ColumnInfo(name = "path_id") val pathId: Long,
    @ColumnInfo(name = "app_name")val appName: String,
    @ColumnInfo(name = "app_package")val appPackage: String,
    @ColumnInfo(name = "time_spent")var timeInForeground: Long,
    @ColumnInfo(name = "n_openings")var nOpenings: Int
 )
