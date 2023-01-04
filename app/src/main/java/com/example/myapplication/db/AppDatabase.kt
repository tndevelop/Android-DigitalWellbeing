package com.example.myapplication.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.db.converters.AppListConverter
import com.example.myapplication.db.converters.AppUsageListConverter
import com.example.myapplication.db.converters.DateConverter
import com.example.myapplication.db.converters.MillisListConverter
import com.example.myapplication.db.dao.*
import com.example.myapplication.db.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@Database(entities = [Path::class, InterventionResult::class, BadUsageNotification::class, InactivityNotification::class, ActiveIntervention::class, FWAppUsage::class,
    App::class, User::class, NotificationDate::class], version = 5)
@TypeConverters(AppListConverter::class, DateConverter::class, MillisListConverter::class, AppUsageListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pathDao(): PathDao
    abstract fun resultDao(): InterventionResultDao
    abstract fun usageNotificationDao(): BadUsageNotificationDao
    abstract fun inactivityNotificationDao(): InactivityNotificationDao
    abstract fun excludedAppDao(): AppDao
    abstract fun activeInterventionDao(): ActiveInterventionDao
    abstract fun fwUsageDao(): FWAppUsageDao
    abstract fun userDao(): UserDao
    abstract fun notificationDateDao(): NotificationDateDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "app_database.db"

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                                .fallbackToDestructiveMigration()

                                //DB POPULATION 4 TESTING
                                /*
                                .addCallback(object : Callback() {
                                    override fun onCreate(db: SupportSQLiteDatabase) {
                                        super.onCreate(db)
                                        Log.d("AppDatabase", "populating with data...")
                                        GlobalScope.launch(Dispatchers.IO) { populateDatabase(INSTANCE) }
                                    }
                                })*/



                                .build()
                    }
                }
            }

            return INSTANCE!!
        }

        //FOR TESTING PURPOSES
        suspend fun populateDatabase(database: AppDatabase?) {
            database?.let { db ->
                withContext(Dispatchers.IO) {
                    val pathDao: PathDao = db.pathDao()
                    val app1 = App("YouTube", "com.google.android.youtube")
                    val app2 = App("Google Chrome", "com.android.chrome")
                    val list = arrayListOf(app1)
                    val list1 = arrayListOf(app2)
                    val appUsage1 = AppUsage(10, "com.google.android.youtube")
                    val appUsage2 = AppUsage(15, "com.android.chrome")
                    val usageList = arrayListOf(appUsage1, appUsage2)
                    val usageList1 = arrayListOf(appUsage1)
                    var path = Path(2, 1, list, Calendar.getInstance(), 0, 0, 1, usageList, true, true)
                    pathDao.addPath(path)
                    path = Path(2, 1, list1, Calendar.getInstance(), 0, 0, 1, usageList1, true, true)
                    pathDao.addPath(path)
                }
            }
        }


        /*

        /**
         * Add a PhoneEvent in the database
         *
         * @param event the detected [PhoneEvent]
         * @param context
         */
        fun addPhoneEvent(event: PhoneEvent?, context: Context) {
            getDatabase(context).phoneModel().insertEvent(event)
        }

        /**
         * Get all the registered [PhoneEvent]
         *
         * @param start initial timestampCreation
         * @param end final timestampCreation
         * @param context
         * @return a list of [PhoneEvent] in a LiveData wrapper
         */
        fun getPhoneEvents(start: Long, end: Long, context: Context): LiveData<List<PhoneEvent?>?>? {
            return getDatabase(context).phoneModel().getPhoneEvents(start, end)
        }

        /**
         * Get all the registered [PhoneEvent]
         * @param start initial timestampCreation
         * @param end final timestampCreation
         * @param context
         * @return a list of [PhoneEvent]
         */
        fun getRawPhoneEvents(start: Long, end: Long, context: Context): List<PhoneEvent?>? {
            return getDatabase(context).phoneModel()
                .getRawPhoneEvents(start, end)
        }

        /**
         * Get the first registered [PhoneEvent]
         *
         * @param context
         * @return a PhoneEvent
         */
        fun getFirstPhoneEventTimestamp(context: Context): Long? {
            return getDatabase(context).phoneModel()
                .getFirstPhoneEventTimestamp()
        }


        /**
         * Add a [NotificationEvent] in the database
         *
         * @param event the detected [NotificationEvent]
         * @param context
         */
        fun addNotificationEvent(event: NotificationEvent?, context: Context) {
            getDatabase(context).notificationModel().insertEvent(event)
        }

        /**
         * Get a [NotificationEvent]
         *
         * @param packageName the package name of the application
         * @param timestampReceived the timestampCreation of the [NotificationEvent]
         * @param context
         * @return
         */
        fun getNotificationEvent(packageName: String?, timestampReceived: Long, context: Context): NotificationEvent? {
            return getDatabase(context).notificationModel().getNotificationEvent(packageName, timestampReceived)
        }


        /**
         * Update a [NotificationEvent]
         *
         * @param event the new [NotificationEvent]
         * @param context
         */
        fun updateNotificationEvent(event: NotificationEvent?, context: Context) {
            getDatabase(context).notificationModel().updateEvent(event)
        }


        /**
         * Get all the registered [NotificationEvent] for a given app
         *
         * @param appName the name of the application
         * @param start initial timestampCreation
         * @param end final timestampCreation
         * @param context
         * @return a list of [NotificationEvent] in a LiveData wrapper
         */
        fun getNotificationEvents(appName: String?, start: Long, end: Long, context: Context): LiveData<List<NotificationEvent?>?>? {
            return getDatabase(context).notificationModel().getAppNotificationEvents(appName, start, end)
        }

        /**
         * Get all the registered [NotificationEvent]
         *
         * @param start initial timestampCreation
         * @param end final timestampCreation
         * @param context
         * @return a list of [NotificationEvent] in a LiveData wrapper
         */
        fun getNotificationEvents(start: Long, end: Long, context: Context): LiveData<List<NotificationEvent?>?>? {
            return getDatabase(context).notificationModel().getNotificationEvents(start, end)
        }

        /**
         * Get all the registered [NotificationEvent]
         *
         * @param start initial timestampCreation
         * @param end final timestampCreation
         * @param context
         * @return a list of [NotificationEvent]
         */
        fun getRawNotificationEvents(start: Long, end: Long, context: Context): List<NotificationEvent?>? {
            return getDatabase(context).notificationModel().getRawNotificationEvents(start, end)
        }

        /**
         * Get all the registered [NotificationEvent] for a given app
         *
         * @param packageName the package name of the app
         * @param start initial timestampCreation
         * @param end final timestampCreation
         * @param context
         * @return a list of [NotificationEvent]
         */
        fun getRawNotificationEvents(packageName: String?, start: Long, end: Long, context: Context): List<NotificationEvent?>? {
            return getDatabase(context).notificationModel().getRawNotificationEvents(packageName, start, end)
        }

        */


    }
}



