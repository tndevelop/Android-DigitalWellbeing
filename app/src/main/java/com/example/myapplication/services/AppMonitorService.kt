package com.example.myapplication.services

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageEvents
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.collection.arrayMapOf
import androidx.lifecycle.LifecycleService
import com.example.myapplication.R
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.data.ActiveIntervention
import com.example.myapplication.db.data.Path
import com.example.myapplication.managers.AlarmsManager
import com.example.myapplication.managers.UsageStatManager
import com.example.myapplication.utils.Constants
import com.example.myapplication.viewModels.HomeViewModel
import java.util.*


class AppMonitorService : LifecycleService() {

    private val usageManager = UsageStatManager()
    private lateinit var context: Context

    private var pathList = arrayListOf<Path>()
    private var activeInt2Apps = arrayListOf<String>()
    private var appMap = arrayMapOf<String, Long>()

    private var opened = false
    private var openedPathId = -1L

    private var actualAppOpen = ""
    private var openCount = 0

    //int2 service
    var myService: Intervention2Service? = null
    var isBound = false
    private var actualActiveInt: ActiveIntervention? = null
    private lateinit var interventionPath: Path
    private lateinit var viewModel: HomeViewModel
    private var penalty = false
    private var end = false
    private var closingSequenceStarted = false
    private var closingSequenceStartedJustSet = false

    //trigger notifications
    private val alarmManager = AlarmsManager()

    override fun onCreate() {
        Log.v("MONITOR_SERVICE", "CREATED")
        context = applicationContext
        viewModel = HomeViewModel(applicationContext as Application)
        startNotification()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.v("MONITOR_SERVICE", "STARTED")

        AppDatabase.getDatabase(context).pathDao().getPathListIntV2().observe(this) { pathList ->
            this.pathList.clear()
            this.pathList.addAll(pathList)
            Log.v("MONITOR_SERVICE", "INTLIST: ${this.pathList}")

            appMap.clear()
            this.pathList.forEach { element ->
                if(element.intervention == Constants.INTERVENTION_2)
                    appMap[element.appList[0].appPackage] = element.id                              //list of apps of int2
            }
            Log.v("MONITOR_SERVICE", "PATHLIST: $appMap")

            AppDatabase.getDatabase(context).activeInterventionDao().getActiveInterventions()
                .observe(this) { intList ->
                    activeInt2Apps.clear()
                    //Log.v("MONITOR_SERVICE", "ACTIVE_INT: $intList")

                    intList.forEach { element ->
                        if (!element.paused && element.int_type == Constants.INTERVENTION_2)
                            this.pathList.forEach { path ->
                                if (path.id == element.intervention_id)
                                    activeInt2Apps.add(path.appList[0].appPackage)                      //list of apps in active int2
                            }
                    }
                     Log.v("MONITOR_SERVICE", "ACTIVE_INT_LIST: $activeInt2Apps")
                }

        }


        val task = object: TimerTask() {
            override fun run() {
                Log.v("MONITOR_SERVICE", "TASK_RUN")

                opened = false
                val time = System.currentTimeMillis()
                val list = usageManager.getAppsEvents(context, pathList.map { it.appList[0].appPackage }, time - Constants.MSEC_OF_POLLING, time)
                var eventList = mutableMapOf<String, MutableList<Int>>()
                list.forEach { event ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        var actualList = eventList.get(event.packageName)
                        if (actualList != null) {
                            actualList.add(event.eventType)
                        }else{
                            actualList = mutableListOf(event.eventType)
                        }
                        eventList.put(event.packageName, actualList)



                        /*
                        Log.d("MONITOR_SERVICE_DEBUG", "${event.eventType} ${event.shortcutId} ${event.appStandbyBucket} ${event.className} ${event.appStandbyBucket} ${event.packageName}")
                        if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                            if(appMap.keys.contains(event.packageName) && !activeInt2Apps.contains(event.packageName)) {             //app in int2, but not in active int2
                            //if(appMap.keys.contains(event.packageName) /*&& !activeInt2Apps.contains(event.packageName)*/) {       //VERSION 10 - experimental<
                                Log.v("MONITOR_SERVICE", "${event.packageName}: APP_OPEN")
                                setOpened(appMap[event.packageName]!!, event.packageName)
                            }
                        } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                        //} else if (event.eventType == UsageEvents.Event.ACTIVITY_STOPPED) { //VERSION 10 - experimental<
                            if(appMap.keys.contains(event.packageName)) {
                                Log.v("MONITOR_SERVICE", "${event.packageName}: APP_CLOSE")
                                resetOpened()
                            }
                        }*/
                    } else {
                        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            if(appMap.keys.contains(event.packageName) && !activeInt2Apps.contains(event.packageName)) {
                                Log.v("MONITOR_SERVICE", "${event.packageName}: APP_OPEN")
                                setOpened(appMap[event.packageName]!!, event.packageName)
                            }
                        } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                            if(appMap.keys.contains(event.packageName)) {
                                Log.v("MONITOR_SERVICE", "${event.packageName}: APP_CLOSE")
                                resetOpened()
                            }
                        }
                    }
                }
                for(appEvent in eventList){
                    var tmpList = appEvent.value

                    while(tmpList.size != 0) {
                        Log.v("MONITOR_SERVICE", "tmp size: ${tmpList.size}")
                        Log.v("MONITOR_SERVICE_IMP", "tmp list: ${appEvent.key} ${tmpList}")
                        if (tmpList[0] == UsageEvents.Event.ACTIVITY_RESUMED) {
                            //app opened
                            Log.v("MONITOR_SERVICE_IMP", "act resumed: $activeInt2Apps")
                            Log.v("MONITOR_SERVICE_IMP", "act resumed: ${appMap.keys}")
                            if (appMap.keys.contains(appEvent.key) /*&& !activeInt2Apps.contains(appEvent.key)*/) {             //app in int2, but not in active int2
                                //if(appMap.keys.contains(appEvent.key) /*&& !activeInt2Apps.contains(appEvent.key)*/) {       //VERSION 10 - experimental<
                                Log.v("MONITOR_SERVICE_IMP", "${appEvent.key}: APP_OPEN")
                                setOpened(appMap[appEvent.key]!!, appEvent.key)
                            }
                            tmpList.removeAt(0)
                        }else if (tmpList.size >= 2 && tmpList[0] == UsageEvents.Event.ACTIVITY_PAUSED && tmpList[1] == UsageEvents.Event.ACTIVITY_STOPPED) {
                            //app closed
                            closeService2(appEvent)
                            tmpList.removeAt(1)
                            tmpList.removeAt(0)
                        } else if (tmpList.size >= 3 && tmpList[0] == UsageEvents.Event.ACTIVITY_PAUSED && tmpList[1] == UsageEvents.Event.ACTIVITY_RESUMED && tmpList[2] == UsageEvents.Event.ACTIVITY_STOPPED) {
                            //change activity inside app
                            tmpList.removeAt(2)
                            tmpList.removeAt(1)
                            tmpList.removeAt(0)
                        } else if (tmpList.size >= 2 && tmpList[0] == UsageEvents.Event.ACTIVITY_PAUSED && tmpList[1] == UsageEvents.Event.ACTIVITY_RESUMED) {
                            tmpList.removeAt(1)
                            tmpList.removeAt(0)
                        }else if (tmpList[0] == UsageEvents.Event.ACTIVITY_PAUSED){
                            closingSequenceStarted = true
                            closingSequenceStartedJustSet = true
                            tmpList.removeAt(0)
                        }
                        else if (tmpList[0] == UsageEvents.Event.ACTIVITY_STOPPED) {
                            if(closingSequenceStarted){
                                // app close event sequence was split between 2 different runs
                                closeService2(appEvent)
                            }
                            tmpList.removeAt(0)
                        }else{
                            //if unrecognized sequence close service just in case
                            closeService2(appEvent)

                            break;
                        }
                    }
                }
                if(!closingSequenceStartedJustSet) closingSequenceStarted = false
                closingSequenceStartedJustSet = false
                /*if(opened)
                    openInt2Popup(openedPathId)*/
            }
        }
        val timer = Timer()
        timer.scheduleAtFixedRate(task, 0,  (Constants.MSEC_OF_POLLING).toLong())

        return START_STICKY
    }

    private fun closeService2(appEvent: MutableMap.MutableEntry<String, MutableList<Int>>) {
        if (appMap.keys.contains(appEvent.key)) {
            Log.v("MONITOR_SERVICE", "${appEvent.key}: APP_CLOSE")
            resetOpened()
        }
    }

    private fun resetNotificationsAlarm(){
        //if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 20){
            alarmManager.setNotificationAlarm(context, Constants.TODAY)
        //}
    }

    fun setOpened(id: Long, packageName: String) {
        resetNotificationsAlarm()
        interventionPath = pathList.filter{it.id == id}[0]
        if(!opened)
            startServiceInt2(packageName)
        opened = true
        openedPathId = id
    }

    fun resetOpened() {
        //VERSION 10 - experimental
        //openCount--
        //if(openCount == 0) {
            stopServiceInt2()
            opened = false
        //}

    }
/*
    private fun openInt2Popup(id: Long) {
        Log.v("MONITOR_SERVICE", "OPEN_POPUP: $id")

        val bundle = bundleOf("PATH_ID" to id, "PENALTY" to true)
        val pendingIntent = NavDeepLinkBuilder(applicationContext)
                .setComponentName(HomeActivity::class.java)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(R.id.nav_home_paths)
                .setArguments(bundle)
                .createPendingIntent()
        pendingIntent.send()
    }*/

    private fun startNotification() {
        val channel = NotificationChannel(Constants.FOREGROUND_CHANNEL_ID, getString(R.string.notification_default_foreground_name), NotificationManager.IMPORTANCE_LOW).apply {
            description = getString(R.string.notification_default_foreground_description)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        viewModel.initPermanentNotification(applicationContext)
        startForeground(Constants.DEFAULT_FOREGROUND, viewModel.notification)
    }

    private val myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as Intervention2Service.LocalBinder
            myService = binder.getService()
            isBound = true
            if(actualActiveInt == null) {
                myService?.startTimer(interventionPath.appList[0], interventionPath.int_duration, interventionPath.level, penalty, interventionPath.id, interventionPath.hasVibration, interventionPath.hasDisplayModification)
                viewModel.addActiveIntervention(interventionPath, Constants.INTERVENTION_2, { /*openApp()*/ }, {})
            }
        }
        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    /*private fun openApp() {
        val intent = packageManager.getLaunchIntentForPackage(interventionPath.appList[0].appPackage)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }*/

    private fun startServiceInt2(packageName: String) {
        Log.d("MONITOR_SERVICE_IMP", "START SERVICE 2")

        //VERSION 10 - experimental
        /*
        if(actualAppOpen != packageName) {
            actualAppOpen = packageName
            openCount = 0
        }
        openCount ++
         */
        viewModel.initPathDeferred(interventionPath)
        val startIntent = Intent(applicationContext, Intervention2Service::class.java)
        startService(startIntent)
        isBound = bindService(startIntent,  myConnection, Context.BIND_AUTO_CREATE)
    }

    private fun stopServiceInt2() {

        /*if (!end) {
            val startIntent =
                Intent(this, Intervention2Service::class.java)
            bindService(
                startIntent,
                myConnection,
                Context.BIND_AUTO_CREATE
            )
        }*/
        Log.d("MONITOR_SERVICE_IMP", "STOP SERVICE 2")
        val result = myService?.stopTimer()
        myService?.destroyNotification()
        val intent = Intent(applicationContext, Intervention2Service::class.java)
        if(result != null) {


            if(isBound) {
                unbindService(myConnection)
                isBound= false
                viewModel.setInt2Result(result!!.startPenalty, result.endPenalty, result.delayPenalty, result.secondsDelay, result.duration, { operationSuccess() }, { operationFailure() }, true)
            }

            end = true
            Log.d("MONITOR_SERVICE", "Result: $result")
        }
        stopService(intent)
    }

    private fun operationSuccess() {
        //navController.navigate(R.id.action_nav_intervention2_to_nav_intervention2_result)
        viewModel.removeActiveIntervention(interventionPath, {}, {})

    }

    private fun operationFailure() {
        //Toast.makeText(requireContext(), "Database error", Toast.LENGTH_LONG).show()
    }

}