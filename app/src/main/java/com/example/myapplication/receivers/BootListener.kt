package com.example.myapplication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myapplication.managers.AlarmsManager
import com.example.myapplication.services.AppMonitorService
import com.example.myapplication.utils.Constants

class BootListener : BroadcastReceiver() {

    private val alarmManager = AlarmsManager()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.v("APP", "BOOT_LISTENER")

            if (context != null) {
                alarmManager.setNotificationAlarm(context, Constants.TODAY)
                alarmManager.setUploadAlarm(context, Constants.TODAY)
            }

            val startIntent = Intent(context, AppMonitorService::class.java)
            context!!.startForegroundService(startIntent)
        }
    }


}