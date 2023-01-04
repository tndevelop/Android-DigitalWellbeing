package com.example.myapplication.db.converters

import androidx.room.TypeConverter
import com.example.myapplication.db.data.App

class AppListConverter {
    @TypeConverter
    fun fromString(string: String): ArrayList<App> {
        val list = ArrayList<String>()
        val appList = ArrayList<App>()
        list.addAll(string.split(",").map { it })
        for (str in list) {
            val fields = str.split("-")
            val app = App(fields[0], fields[1])
            appList.add(app)
        }
        return appList
    }

    @TypeConverter
    fun toString(appList: ArrayList<App>): String {
        val stringList = ArrayList<String>()
        for (app in appList) {
            val str = app.appName + "-" + app.appPackage
            stringList.add(str)
        }
        return stringList.joinToString(separator = ",")
    }
}