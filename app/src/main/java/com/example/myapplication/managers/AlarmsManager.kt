package com.example.myapplication.managers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.data.User
import com.example.myapplication.services.NotificationService
import com.example.myapplication.services.UploadUsageService
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.Functions
import com.example.myapplication.viewModels.HomeViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AlarmsManager {

    val functions = Functions()


    fun setNotificationAlarm(context: Context, today_tomorrow: Boolean) {

        Log.v("ALARM_MANAGER", "ALARM_NOTIFICATION")
        val alarmIntent = Intent(context, NotificationService::class.java)
        //var pendingAlarmIntent = PendingIntent.getService(context, Constants.PENDING_INTENT_ALARM_CODE, alarmIntent, PendingIntent.FLAG_NO_CREATE)
        //if(pendingAlarmIntent == null) {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            var pendingAlarmIntent = PendingIntent.getService(context, Constants.PENDING_INTENT_ALARM_CODE, alarmIntent, 0)

            Log.v("ALARM_MANAGER", "ALARM_NOTIFICATION_NEW")
            val calendar: Calendar = Calendar.getInstance()
            if(today_tomorrow) {
                calendar.apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, Constants.HOUR_DAILY_RECAP)
                    set(Calendar.MINUTE, 0)
                }
            }
            else {
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.DAY_OF_MONTH, day+1)
                    set(Calendar.HOUR_OF_DAY, Constants.HOUR_DAILY_RECAP)
                    set(Calendar.MINUTE, 0)
                }
            }

            alarmMgr.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingAlarmIntent
            )


        //}

        //set notification for when monitoring phase will end
        GlobalScope.launch {
            Log.v("ALARM_MANAGER", "ALARM_END_MONITORING")
            var pendingAlarmIntentMonitoring : PendingIntent

            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            pendingAlarmIntentMonitoring = PendingIntent.getService(context, Constants.PENDING_INTENT_ALARM_CODE, alarmIntent, 0)


            val user = AppDatabase.getDatabase(context).userDao().getUser()


            if (user!=null && functions.isMonitoringPhase(user.startDate)) {
                val daysToMonitoringEnd = functions.inNDays(user.startDate, Constants.N_DAYS_MONITORING_PHASE)
                Log.v("ALARM_MANAGER", "monitoring end: ${daysToMonitoringEnd.timeInMillis}")
                alarmMgr.setExact(
                    AlarmManager.RTC_WAKEUP,
                    daysToMonitoringEnd.timeInMillis,
                    pendingAlarmIntentMonitoring
                )
            }

        }

    }



    fun setUploadAlarm(context: Context, today_tomorrow: Boolean) {
        Log.v("ALARM_MANAGER", "ALARM_UPLOAD")
        val alarmIntent = Intent(context, UploadUsageService::class.java)
        var pendingAlarmIntent = PendingIntent.getService(context, Constants.PENDING_INTENT_ALARM_CODE, alarmIntent, PendingIntent.FLAG_NO_CREATE)
        if(pendingAlarmIntent == null) {
            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            pendingAlarmIntent = PendingIntent.getService(context, Constants.PENDING_INTENT_ALARM_CODE, alarmIntent, 0)

            Log.v("ALARM_MANAGER", "ALARM_UPLOAD_NEW")
            val calendar: Calendar = Calendar.getInstance()
            if(today_tomorrow) {
                calendar.apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 55)
                }
            }
            else {
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.DAY_OF_MONTH, day+1)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 55)
                }
            }

            alarmMgr.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingAlarmIntent
            )
        }
    }
}