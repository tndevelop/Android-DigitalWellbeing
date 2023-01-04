package com.example.myapplication.db.converters

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
    @TypeConverter
    fun timestampToCalendar(value: Long): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = value
        return calendar
    }

    @TypeConverter
    fun calendarToTimestamp(calendar: Calendar): Long {
        return calendar.timeInMillis
    }
}
