package com.example.myapplication.db.converters

import androidx.room.TypeConverter
import java.util.*

class MillisListConverter {
    @TypeConverter
    fun listToString(value: ArrayList<Long>): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun stringToList(string: String): ArrayList<Long> {
        return if (string == "")
            arrayListOf()
        else {
            val strList = string.split(",")
            val longList = arrayListOf<Long>()
            for (str in strList)
                longList.add(str.toLong())
            longList
        }
    }
}