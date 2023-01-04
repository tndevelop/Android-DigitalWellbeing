package com.example.myapplication.db.data

import android.app.usage.UsageEvents
import android.graphics.drawable.Drawable

data class CustomUsageStats(
    var timeInForeground: Long,
    var eventList: List<UsageEvents.Event>,
    var nOpenings: Int,
    var appPackage: String,
    var appName: String = "",
    var appIcon: Drawable? = null,
    var category: String
)
