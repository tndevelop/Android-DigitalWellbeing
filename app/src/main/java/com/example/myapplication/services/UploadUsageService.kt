package com.example.myapplication.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.managers.AlarmsManager
import com.example.myapplication.managers.UsageStatManager
import com.example.myapplication.managers.WorkManagerInterface
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.Functions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UploadUsageService: Service() {

    private val functions = Functions()
    private val workUploadManager = WorkManagerInterface()
    private val usageStatManager = UsageStatManager()
    private val externalScope: CoroutineScope = GlobalScope

    override fun onCreate() {
        Log.v("SERVICE_USAGE_UP", "SERVICE_CREATE")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("SERVICE_USAGE_UP", "SERVICE_START")

        val time = System.currentTimeMillis()
        val usageList = usageStatManager.getPhoneUsageDB(applicationContext, time - (24 * Constants.HOUR_TO_MSEC), time)
        usageList.forEach { usageDB ->
            Log.v("SERVICE_USAGE_UP", "STAT: $usageDB")
            externalScope.launch {
                val user = AppDatabase.getDatabase(applicationContext).userDao().getUser()
                workUploadManager.usageUpload(usageDB, user.userId, applicationContext, false)
            }
        }

        val alarmManager = AlarmsManager()
        alarmManager.setUploadAlarm(applicationContext, Constants.TOMORROW)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}
