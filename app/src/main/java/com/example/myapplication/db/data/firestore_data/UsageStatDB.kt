package com.example.myapplication.db.data.firestore_data

data class UsageStatDB(
        val appName: String,
        val appPackage: String,
        val date: String,
        val timeInForeground: Long,
        val nOpenings: Int,
        val start: Long,
        val end: Long
)
