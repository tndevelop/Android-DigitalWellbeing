package com.example.myapplication.managers

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.example.myapplication.db.data.CustomUsageStats
import com.example.myapplication.db.data.firestore_data.UsageStatDB
import com.example.myapplication.utils.AppCategory
import com.example.myapplication.utils.AppUsageComparator
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.Functions
import java.util.*

class UsageStatManager {

    val functions = Functions()

    //return a lit of customUsageStat for all device apps
    fun getPhoneUsage(context: Context, start: Long, end: Long): List<CustomUsageStats> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val customUsageStatList: MutableList<CustomUsageStats> = LinkedList()

        val queryUsageStatsMap: Map<String, UsageStats> = usm.queryAndAggregateUsageStats(start, end)
        val usageStatsList: MutableList<UsageStats> = LinkedList()
        for (stats in queryUsageStatsMap.values) {
            //if (stats.lastTimeUsed > start && stats.totalTimeInForeground > 0)
                usageStatsList.add(stats)
        }

        val queryUsageEvent = usm.queryEvents(start, end)
        val usageEventList: MutableList<UsageEvents.Event> = LinkedList()
        while (queryUsageEvent.hasNextEvent()) {
            val event = UsageEvents.Event()
            queryUsageEvent.getNextEvent(event)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.ACTIVITY_PAUSED)
                    usageEventList.add(event)
            } else {
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND)
                    usageEventList.add(event)
            }
        }
        val categoryList = mutableMapOf<String, String>()
        //cycle for each app that have an usage stat
        usageStatsList.forEach { app ->
            var checked = 0

            val activityList: MutableList<String> = LinkedList()
            var category : String = AppCategory.values()[0].toString() // default undefined
            try {
                val pi: PackageInfo = context.packageManager.getPackageInfo(app.packageName, PackageManager.GET_ACTIVITIES)
                category = AppCategory.values()[pi.applicationInfo.category].toString()
                categoryList.put(app.packageName, AppCategory.values()[pi.applicationInfo.category].toString())
                pi.activities.forEach { activity ->
                    activityList.add(activity.name)
                }

            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            } catch (t: Throwable) {
                t.printStackTrace()
            }

            //exclude apps that do not have any activities
            if (activityList.size > 0) {
                val appEventList = getAppEvents(app.packageName, usageEventList)
                Collections.sort(appEventList, AppUsageComparator.TimeStampComparatorAsc())
                var monitor = false
                var usage: Long = 0
                var activityStart: Long = 0
                var activityStop: Long
                var waCloseTimestamp = 0L

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    for (event in appEventList) {
                        if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED && !monitor) {
                            if( ( event.packageName == "com.whatsapp" && (event.timeStamp - waCloseTimestamp) > Constants.WA_THRESHOLD ) || event.packageName != "com.whatsapp") {
                                checked += 1
                            }
                            monitor = true
                            activityStart = event.timeStamp
                        } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED && monitor) {
                            if(event.packageName == "com.whatsapp")
                                waCloseTimestamp = event.timeStamp
                            monitor = false
                            activityStop = event.timeStamp
                            if (activityStop > activityStart) usage += activityStop - activityStart
                        }
                    }
                } else {
                    for (event in appEventList) {
                        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && !monitor) {
                            if( ( event.packageName == "com.whatsapp" && (event.timeStamp - waCloseTimestamp) > Constants.WA_THRESHOLD ) || event.packageName != "com.whatsapp") {
                                checked += 1
                            }
                            monitor = true
                            activityStart = event.timeStamp
                        } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND && monitor) {
                            if(event.packageName == "com.whatsapp")
                                waCloseTimestamp = event.timeStamp
                            monitor = false
                            activityStop = event.timeStamp
                            if (activityStop > activityStart) usage += activityStop - activityStart
                        }
                    }
                }

                val pm: PackageManager = context.packageManager
                val cUsageStat = CustomUsageStats(usage, appEventList, checked, app.packageName,
                        pm.getApplicationLabel(pm.getApplicationInfo(app.packageName, 0)).toString(),
                        pm.getApplicationIcon(app.packageName),
                        category)

                customUsageStatList.add(cUsageStat)

            }
        }
        return customUsageStatList
    }

    //return a customUsageStat data for a specific app
    fun getAppUsage(context: Context, packageName: String, start: Long, end: Long): CustomUsageStats {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        //get app category
        var category : String = AppCategory.values()[0].toString() // default undefined
        val queryUsageStatsMap: Map<String, UsageStats> = usm.queryAndAggregateUsageStats(start, end)
        var app: UsageStats
        for (stats in queryUsageStatsMap.values) {
            if (stats.packageName == packageName) {
                app = stats
                try {
                    val pi: PackageInfo = context.packageManager.getPackageInfo(app.packageName, PackageManager.GET_ACTIVITIES)
                    category = AppCategory.values()[pi.applicationInfo.category].toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }

        val events = usm.queryEvents(start, end)
        val eventList: MutableList<UsageEvents.Event> = LinkedList()
        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            if (event.packageName == packageName) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.ACTIVITY_PAUSED)
                        eventList.add(event)
                } else {
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND)
                        eventList.add(event)
                }
            }
        }


        Collections.sort(eventList, AppUsageComparator.TimeStampComparatorAsc())
        var checked = 0

        //get accurate usage time and checking events
        var monitor = false
        var usage: Long = 0
        var activityStart: Long = 0
        var activityStop: Long
        var waCloseTimestamp = 0L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            for (event in eventList) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED && !monitor) {
                    if( ( event.packageName == "com.whatsapp" && (event.timeStamp - waCloseTimestamp) > Constants.WA_THRESHOLD ) || event.packageName != "com.whatsapp") {
                        checked += 1
                    }
                    monitor = true
                    activityStart = event.timeStamp
                } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED && monitor) {
                    if(event.packageName == "com.whatsapp")
                        waCloseTimestamp = event.timeStamp
                    monitor = false
                    activityStop = event.timeStamp
                    if (activityStop > activityStart) usage += activityStop - activityStart
                }
            }
        } else {
            for (event in eventList) {
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && !monitor) {
                    if( ( event.packageName == "com.whatsapp" && (event.timeStamp - waCloseTimestamp) > Constants.WA_THRESHOLD ) || event.packageName != "com.whatsapp") {
                        checked += 1
                    }
                    monitor = true
                    activityStart = event.timeStamp
                } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND && monitor) {
                    if(event.packageName == "com.whatsapp")
                        waCloseTimestamp = event.timeStamp
                    monitor = false
                    activityStop = event.timeStamp
                    if (activityStop > activityStart) usage += activityStop - activityStart
                }
            }
        }

        val pm: PackageManager = context.packageManager
        return CustomUsageStats(usage, eventList, checked, packageName,
                pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString(),
                pm.getApplicationIcon(packageName),
                category)
    }

    //return all the events of opening and closing for the apps in the specified list
    fun getAppsEvents(context: Context, list: List<String>, start: Long, end: Long): MutableList<UsageEvents.Event> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val events = usm.queryEvents(start, end)
        val eventList: MutableList<UsageEvents.Event> = LinkedList()
        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            if (list.contains(event.packageName)) {
                Log.d("EVENT_DEBUG", event.packageName + event.eventType.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.ACTIVITY_STOPPED) //VERSION 10 - experimental<
                    //if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.ACTIVITY_PAUSED)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.ACTIVITY_PAUSED || event.eventType == UsageEvents.Event.ACTIVITY_STOPPED)
                        eventList.add(event)
                } else {
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND)
                        eventList.add(event)
                }
            }
            if(!events.hasNextEvent()){
                Log.d("EVENT_DEBUG", "finish")
            }
        }
        Collections.sort(eventList, AppUsageComparator.TimeStampComparatorAsc())
        return eventList
    }

    //get the events of a specified app from a list opf events
    private fun getAppEvents(packageName: String?, usageEventList: MutableList<UsageEvents.Event>): MutableList<UsageEvents.Event> {
        val appUsageEventsList: MutableList<UsageEvents.Event> = LinkedList()
        for (event in usageEventList) {
            if (event.packageName == packageName) {
                appUsageEventsList.add(event)
            }
        }
        return appUsageEventsList
    }


    //get the usage stats of the device for the day to upload on firebase
    fun getPhoneUsageDB(context: Context, start: Long, end: Long): MutableList<UsageStatDB> {

        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStatListDB: MutableList<UsageStatDB> = LinkedList()

        val queryUsageStatsMap: Map<String, UsageStats> = usm.queryAndAggregateUsageStats(start, end)
        val usageStatsList: MutableList<UsageStats> = LinkedList()
        for (stats in queryUsageStatsMap.values) {
            if (stats.lastTimeUsed > start && stats.totalTimeInForeground > 0)
                usageStatsList.add(stats)
        }

        val queryUsageEvent = usm.queryEvents(start, end)
        val usageEventList: MutableList<UsageEvents.Event> = LinkedList()
        while (queryUsageEvent.hasNextEvent()) {
            val event = UsageEvents.Event()
            queryUsageEvent.getNextEvent(event)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.ACTIVITY_PAUSED)
                    usageEventList.add(event)
            } else {
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND)
                    usageEventList.add(event)
            }
        }

        //cycle for each app that have an usage stat
        usageStatsList.forEach { app ->
            var checked = 0

            val activityList: MutableList<String> = LinkedList()
            try {
                val pi: PackageInfo = context.packageManager.getPackageInfo(app.packageName, PackageManager.GET_ACTIVITIES)
                pi.activities.forEach { activity ->
                    activityList.add(activity.name)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            } catch (t: Throwable) {
                t.printStackTrace()
            }

            //exclude apps that do not have any activities
            if (activityList.size > 0) {
                val appEventList = getAppEvents(app.packageName, usageEventList)
                Collections.sort(appEventList, AppUsageComparator.TimeStampComparatorAsc())
                var monitor = false
                var usage: Long = 0
                var activityStart: Long = 0
                var activityStop: Long
                var waCloseTimestamp = 0L

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    for (event in appEventList) {
                        if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED && !monitor) {
                            if( ( event.packageName == "com.whatsapp" && (event.timeStamp - waCloseTimestamp) > Constants.WA_THRESHOLD ) || event.packageName != "com.whatsapp") {
                                checked += 1
                            }
                            monitor = true
                            activityStart = event.timeStamp
                        } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED && monitor) {
                            if(event.packageName == "com.whatsapp")
                                waCloseTimestamp = event.timeStamp
                            monitor = false
                            activityStop = event.timeStamp
                            if (activityStop > activityStart) usage += activityStop - activityStart
                        }
                    }
                } else {
                    for (event in appEventList) {
                        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && !monitor) {
                            if( ( event.packageName == "com.whatsapp" && (event.timeStamp - waCloseTimestamp) > Constants.WA_THRESHOLD ) || event.packageName != "com.whatsapp") {
                                checked += 1
                            }
                            monitor = true
                            activityStart = event.timeStamp
                        } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND && monitor) {
                            if(event.packageName == "com.whatsapp")
                                waCloseTimestamp = event.timeStamp
                            monitor = false
                            activityStop = event.timeStamp
                            if (activityStop > activityStart) usage += activityStop - activityStart
                        }
                    }
                }

                val pm: PackageManager = context.packageManager
                val date = functions.getDateTimeStr(Calendar.getInstance().time)
                val usageDB = UsageStatDB(pm.getApplicationLabel(pm.getApplicationInfo(app.packageName, 0)).toString(),
                        app.packageName, date, usage, checked, start, end)

                usageStatListDB.add(usageDB)

            }
        }
        return usageStatListDB
    }

}