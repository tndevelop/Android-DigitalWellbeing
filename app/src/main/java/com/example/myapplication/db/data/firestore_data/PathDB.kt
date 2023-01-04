package com.example.myapplication.db.data.firestore_data


data class PathDB(
        val id: Long = 0,
        val intervention: Int,
        val level: Int,
        val appList: String,
        var start_date: String,
        val n_interventions: Int,
        val n_miss_interventions: Int,
        val int_duration: Int,
        val appUsageList: String,
        val operationDate: String,
        val operationType: String,
        val hasVibration: Boolean,
        val hasDisplayModification : Boolean,
)


