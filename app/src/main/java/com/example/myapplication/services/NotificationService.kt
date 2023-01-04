package com.example.myapplication.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.myapplication.R
import com.example.myapplication.activities.HomeActivity
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.data.BadUsageNotification
import com.example.myapplication.db.data.CustomUsageStats
import com.example.myapplication.db.data.InactivityNotification
import com.example.myapplication.db.data.NotificationDate
import com.example.myapplication.managers.AlarmsManager
import com.example.myapplication.managers.UsageStatManager
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.Functions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

class NotificationService: Service() {

    private val statManager = UsageStatManager()
    private val functions = Functions()
    private val externalScope: CoroutineScope = GlobalScope


    override fun onCreate() {
        Log.v("SERVICE_NOTIFICATION", "SERVICE_CREATE")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("SERVICE_NOTIFICATION", "SERVICE_START")

        var manual = false
        if(intent != null)
            manual = intent.getBooleanExtra("MANUALLY_STARTED", false)


        externalScope.launch {
            val date = AppDatabase.getDatabase(applicationContext).notificationDateDao().getDate()
            Log.v("SERVICE_NOTIFICATION", "LAST_NOTIFICATION_DATE: $date")

            if(manual)
                suggestionNotification()
            else {
                val today = functions.getDateStr(Calendar.getInstance().time)
                if(date == null || date.date != today) {
                    suggestionNotification()
                    endMonitoringPhaseNotification()
                    recapNotification()
                    AppDatabase.getDatabase(applicationContext).notificationDateDao().deleteDate()
                    AppDatabase.getDatabase(applicationContext).notificationDateDao().setDate(NotificationDate(today))
                }
            }
        }

        val alarmManager = AlarmsManager()
        alarmManager.setNotificationAlarm(applicationContext, Constants.TOMORROW)

        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun recapNotification() {
        Log.v("SERVICE_NOTIFICATION", "END_MONITORING")
        var fwNotification = false
        val paths = AppDatabase.getDatabase(applicationContext).pathDao().getPathList()
        for (path in paths){
            if(functions.checkRecap(path)){
                fwNotification = true
                break;
            }
        }
        if (fwNotification) {
            createNotification(R.id.nav_home_paths, getString(R.string.notification_recap))
        }
    }

    private suspend fun endMonitoringPhaseNotification() {
        Log.v("SERVICE_NOTIFICATION", "END_MONITORING")
        var fwNotification = false
        val user = AppDatabase.getDatabase(applicationContext).userDao().getUser()
        //monitoring period ended by less than 1 day
        if(!functions.isMonitoringPhase(user.startDate) && abs(functions.daysDifference(user.startDate)) < Constants.N_DAYS_MONITORING_PHASE + 1) {
            fwNotification = true
        }
        if (fwNotification) {
            createNotification(R.id.nav_home_paths, getString(R.string.notification_monitoring_phase_ended))
        }
    }

    private suspend fun suggestionNotification() {
        Log.v("SERVICE_NOTIFICATION", "PATH_START_SUGGESTION")
        var usageNotification = false
        val excludedAppList =  AppDatabase.getDatabase(applicationContext).excludedAppDao().getExcludedAppList().map { it.appPackage}
        val pathList = AppDatabase.getDatabase(applicationContext).pathDao().getPathListV1().map { it.appList }
        val pathsAppList = arrayListOf<String>()
        pathList.forEach { list ->
            list.forEach { element ->
                pathsAppList.add(element.appPackage)
            }
        }
        val notificationList = AppDatabase.getDatabase(applicationContext).usageNotificationDao().getAllUsages2().associateBy { it.appPackage }
        val time = System.currentTimeMillis()
        val stats = statManager.getPhoneUsage(applicationContext, time - Constants.DAYS_TO_MSEC, time)
        val statsLW = statManager.getPhoneUsage(applicationContext, functions.getMsec7DaysAgo(), time)

        val user = AppDatabase.getDatabase(applicationContext).userDao().getUser()
        if(!functions.isMonitoringPhase(user.startDate)) {
            stats.forEach {
                if (!excludedAppList.contains(it.appPackage) && !pathsAppList.contains(it.appPackage) && it.appPackage != packageName) {
                    if (functions.dangerousApp(it, statsLW)) {
                        usageNotification = true
                        if (notificationList.keys.contains(it.appPackage)) {
                            updateOldUsageNotification(it, notificationList[it.appPackage]!!)
                        } else createNewUsageNotification(it)
                    }
                }
            }
            if (usageNotification) {
                createNotification(R.id.nav_notification_list, getString(R.string.notification_path_suggestion))
            }
        }
    }



    private suspend fun updateOldUsageNotification(newStat: CustomUsageStats, oldNotification: BadUsageNotification) {
        val newNotification = BadUsageNotification(oldNotification.appPackage, oldNotification.appName, Calendar.getInstance(), newStat.timeInForeground, newStat.nOpenings, false)
        AppDatabase.getDatabase(applicationContext).usageNotificationDao().updateUsage(newNotification)
        Log.v("SERVICE", "USAGE_OLD: $newNotification")
    }

    private suspend fun createNewUsageNotification(stat: CustomUsageStats) {
        val newNotification = BadUsageNotification(stat.appPackage, stat.appName, Calendar.getInstance(), stat.timeInForeground, stat.nOpenings, false)
        AppDatabase.getDatabase(applicationContext).usageNotificationDao().addUsage(newNotification)
        Log.v("SERVICE", "USAGE_NEW: $newNotification")
    }

    override fun onDestroy() {
        Log.v("SERVICE_NOTIFICATION", "SERVICE_DESTROY")
        super.onDestroy()
    }

    private fun createNotification(destination: Int, title: String) {
        val channel = NotificationChannel(Constants.DEFAULT_CHANNEL_ID, getString(R.string.notification_default_name), NotificationManager.IMPORTANCE_HIGH).apply {
            description = getString(R.string.notification_default_description)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        val id = Random().nextInt(300)
        val pendingIntent = NavDeepLinkBuilder(applicationContext)
                .setComponentName(HomeActivity::class.java)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(destination)
                .createPendingIntent()
        val notification: Notification = NotificationCompat.Builder(this, Constants.DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        with(NotificationManagerCompat.from(this)) {
            notify(id, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}