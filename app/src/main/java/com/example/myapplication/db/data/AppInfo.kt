package com.example.myapplication.db.data

import android.graphics.drawable.Drawable

data class AppInfo(
        val appName: String,
        val appPackage: String,
        val icon: Drawable,
        var selected: Boolean = false
)