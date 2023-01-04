package com.example.myapplication.db.data

import java.util.*

data class Notification(
        val type: Int,
        val usage: BadUsageNotification? = null,
        val inactivity: InactivityNotification? = null,
        val date: Calendar
)
