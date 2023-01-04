package com.example.myapplication.db.converters

import androidx.room.TypeConverter
import com.example.myapplication.db.data.App
import com.example.myapplication.db.data.AppUsage

class AppUsageListConverter {
    @TypeConverter
    fun fromString(string: String): ArrayList<AppUsage> {
        val list = ArrayList<String>()
        val appUsageList = ArrayList<AppUsage>()
        list.addAll(string.split(",").map { it })
        for (str in list) {
            val appUsage : AppUsage
            if(str != "") {
                val fields = str.split("-")
                appUsage = AppUsage(fields[0].toInt(), fields[1])
            }else{
                appUsage = AppUsage(1, "com.google.android.youtube")
            }
            appUsageList.add(appUsage)
        }
        return appUsageList
    }

    @TypeConverter
    fun toString(appUsageList: ArrayList<AppUsage>): String {
        val stringList = ArrayList<String>()
        for (appUsage in appUsageList) {
            val str = appUsage.avgUsage.toString() + "-" + appUsage.appPackage
            stringList.add(str)
        }
        return stringList.joinToString(separator = ",")
    }
}