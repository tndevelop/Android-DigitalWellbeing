package com.example.myapplication.viewModels

import android.app.Application
import android.app.Notification
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDeepLinkBuilder
import com.example.myapplication.FirebaseUrlsService
import com.example.myapplication.R
import com.example.myapplication.Rasa.MessageClass
import com.example.myapplication.activities.HomeActivity
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.dao.*
import com.example.myapplication.db.data.*
import com.example.myapplication.db.data.firestore_data.ChatDB
import com.example.myapplication.db.data.firestore_data.InterventionResultDB
import com.example.myapplication.db.data.firestore_data.PathDB
import com.example.myapplication.managers.UsageStatManager
import com.example.myapplication.managers.WorkManagerInterface
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.Functions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val database = AppDatabase.getDatabase(application)
    private val pathDao: PathDao = database.pathDao()
    private val resultDao: InterventionResultDao = database.resultDao()
    private val badUsageDao: BadUsageNotificationDao = database.usageNotificationDao()
    private val inactivityDao = database.inactivityNotificationDao()
    private val excludedAppDao: AppDao = database.excludedAppDao()
    private val activeInterventionDao: ActiveInterventionDao = database.activeInterventionDao()
    private val fwUsageDao: FWAppUsageDao = database.fwUsageDao()
    private val userDao: UserDao = database.userDao()
    private val workInterface = WorkManagerInterface()

    private val functions = Functions()

    private val installedAppList = MutableLiveData<List<AppInfo>>()                                 //list of recently used apps
    private var pathList : LiveData<List<Path>>                                                     //list of active paths
    private var selectedPath = MutableLiveData<Path> ()
    private lateinit var pathResultList: LiveData<List<InterventionResult>>                         //list of results of a single path
    private var activeInterventionList: LiveData<List<ActiveIntervention>>                          //list of interventions currently active
    private var usageNotificationList: LiveData<List<BadUsageNotification>>                         //list of app's usage notifications
    private var inactivityNotificationList: LiveData<List<InactivityNotification>>                  //list of app's inactivity notifications
    private var selectedUsageNotification = MutableLiveData<BadUsageNotification> ()
    private var selectedInactivityNotification = MutableLiveData<InactivityNotification> ()
    private lateinit var intervention1AppList: ArrayList<App>                                       //apps marked for the intervention 1.2
    private lateinit var interventionResult: InterventionResult                                     //result of a concluded intervention
    private lateinit var appStats: CustomUsageStats                                                 //statistics of an app usage in the last hours
    var currentUser: LiveData<User>
    var startOfDayStatList = MutableLiveData<List<CustomUsageStats>>()
    //chat
    var messageList: MutableList<MessageClass> = mutableListOf()
    var nPathChatbot : Int = 0
    var dangerousApps = mutableListOf<SuggestionOnApp>()
    var statList = MutableLiveData<List<CustomUsageStats>>()
    var lastWeekUsage = MutableLiveData<List<CustomUsageStats>>()
    private val statManager = UsageStatManager()
    lateinit var chatbotBaseUrl: String

    //permanent notification
    lateinit var notification : Notification

    //Get the user-installed apps on the phone and initialize the list
    fun initInstalledApps(context: Context, myApp: String) {
        GlobalScope.launch {
            val list = ArrayList<AppInfo>()
            val installedApps: MutableList<ApplicationInfo> = ArrayList()
            val packageList = ArrayList<String> ()

            val pm: PackageManager = context.packageManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val mainIntent = Intent(Intent.ACTION_MAIN, null)
                val pkgAppsList = context.packageManager.queryIntentActivities(mainIntent, 0)
                pkgAppsList.forEach { app ->
                    when {
                        (app.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 -> {
                            packageList.add(app.activityInfo.packageName)
                        }
                        (app.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 -> {
                            //discard this one
                        }
                        else ->{
                            packageList.add(app.activityInfo.packageName)
                        }
                    }
                }
                packageList.distinct().forEach {
                    val appInfo: ApplicationInfo? = try {
                        pm.getApplicationInfo(it, 0)
                    } catch (exception: PackageManager.NameNotFoundException){
                        null
                    }
                    if(appInfo!= null)
                        installedApps.add(appInfo)
                }
            }

            else {
                val apps = pm.getInstalledApplications(0)
                apps.forEach{ app ->
                    when {
                        (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 -> {
                            installedApps.add(app)
                        }
                        (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0 -> {
                            //Discard this one
                        }
                        else -> {
                            installedApps.add(app)
                        }
                    }
                }
            }

            installedApps.forEach {
                val app = AppInfo(pm.getApplicationLabel(it).toString(), it.packageName, it.loadIcon(pm))
                if (app.appName != myApp && !list.contains(app))  {
                    list.add(app)
                }
            }
            list.sortBy { element -> element.appName }
            withContext(Dispatchers.Main) { installedAppList.postValue(list) }
        }
    }


    fun getInstalledApp(): LiveData<List<AppInfo>> {
        return installedAppList
    }

    fun uploadUserID(uid: String) {
        viewModelScope.launch {
            try{
                val dao = AppDatabase.getDatabase(appContext).userDao()
                val user = dao.getUser()
                if(user == null) {
                    dao.deleteUser()
                    dao.setUser(User(uid, Calendar.getInstance()))
                }
            }
            catch (exception: Exception) {
                Log.v("DATABASE", "USER_UPLOAD")
            }
        }
    }

    fun deleteUser(){
        viewModelScope.launch{
            val dao = AppDatabase.getDatabase(appContext).userDao()
            dao.deleteUser()
        }
    }



    init {
        currentUser = userDao.getUserV2()
        pathList = pathDao.getPathListV2()
        usageNotificationList = badUsageDao.getAllUsages()
        activeInterventionList = activeInterventionDao.getActiveInterventions()
        inactivityNotificationList = inactivityDao.getNotificationV1()
        initChatbotUrl()
    }

    private fun initChatbotUrl(){
        viewModelScope.launch {
            FirebaseUrlsService.getChatbotUrl(this@HomeViewModel)
        }

    }


    private fun updatePathList() {
        pathList = pathDao.getPathListV2()
    }

    fun getPathList(): LiveData<List<Path>> {
        return pathList
    }

    fun initPath(path: Path) {
        selectedPath.value = path
    }

    fun initPathDeferred(path: Path) {
        selectedPath.postValue(path)
    }

    fun getPath(): MutableLiveData<Path> {
        return selectedPath
    }

    fun initPathResults(pathId: Long){
        pathResultList = resultDao.getPathResults(pathId)
    }

    fun getPathResults(): LiveData<List<InterventionResult>> {
        return pathResultList
    }

    private fun updateActiveIntList() {
        activeInterventionList = activeInterventionDao.getActiveInterventions()
    }

    fun getActiveInterventions(): LiveData<List<ActiveIntervention>> {
        return activeInterventionList
    }

    private fun updateNotificationList() {
        usageNotificationList = badUsageDao.getAllUsages()
        inactivityNotificationList = inactivityDao.getNotificationV1()
    }

    fun getUNotificationList(): LiveData<List<BadUsageNotification>> {
        return usageNotificationList
    }

    fun initUNotification(usage: BadUsageNotification) {
        selectedUsageNotification.value = usage
    }

    fun setAppStats(stats: CustomUsageStats) {
        appStats= stats
    }

    fun getAppStats(): CustomUsageStats {
        return appStats
    }

        //Create a new path
    fun addNewPath(app: App, operationSuccess: () -> Unit, operationFailure: () -> Unit, suggestion : SuggestionOnApp = SuggestionOnApp()) {
        val list = arrayListOf(app)
            //TODO fix empty usageList
        var usageList = arrayListOf<AppUsage>()
        val path = Path(0, 0, list, Calendar.getInstance(), 0, 0, suggestion.suggestedTime.toInt(), usageList, suggestion.vibrationSuggested, suggestion.greyOutSuggested)
        viewModelScope.launch {
            try{
                val userId = userDao.getUser().userId
                val pid = pathDao.addPath(path)
                path.id = pid
                val pathDB = createPathDB(path, Constants.FIREBASE_CREATION)
                workInterface.pathUpload(pathDB, userId, appContext, false)
                operationSuccess()
            }
            catch (exception: Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

    fun removePath(path: Path, operationSuccess: () -> Unit, operationFailure: () -> Unit) {
        viewModelScope.launch {
            try{
                val userId = userDao.getUser().userId
                pathDao.deletePath(path)
                val pathDB = createPathDB(path, Constants.FIREBASE_DELETE)
                workInterface.pathUpload(pathDB, userId, appContext, false)
                operationSuccess()
            }
            catch (exception: Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

    fun updatePath(path: Path, operationSuccess: () -> Unit, operationFailure: () -> Unit) {
        viewModelScope.launch {
            try{
                val userId = userDao.getUser().userId
                pathDao.updatePath(path)
                val pathDB = createPathDB(path, Constants.FIREBASE_UPDATE)
                workInterface.pathUpload(pathDB, userId, appContext, false)
                operationSuccess()
            }
            catch (exception: Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

        //After the first week of monitoring, upgrade the blank path to type 2
    fun upgradePathFWto2(
            path: Path,
            usage: FWAppUsage?,
            operationSuccess: () -> Unit,
            operationFailure: () -> Unit
        ) {
        path.intervention = Constants.INTERVENTION_2
        path.n_interventions = 0
        path.n_miss_interventions = 0
        path.start_date = Calendar.getInstance()
        path.level = 1
        viewModelScope.launch {
            try{
                val userId = userDao.getUser().userId
                if (usage != null) { fwUsageDao.deleteFWUsage(usage) }
                pathDao.updatePath(path)
                //stop uploading path as FW_ENDED
                //val pathDB = createPathDB(path, Constants.FIREBASE_FW_END)
                //workInterface.pathUpload(pathDB, userId, appContext, false)
                operationSuccess()
            }
            catch (exception: Exception) {
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

        //Update path info after a level change
    fun pathLevelUpdate(path: Path, operationSuccess: () -> Unit, operationFailure: () -> Unit) {
        viewModelScope.launch {
            try{
                val userId = userDao.getUser().userId
                path.n_interventions = 0
                path.n_miss_interventions = 0
                path.start_date = Calendar.getInstance()
                resultDao.deletePathResult(path.id)
                pathDao.updatePath(path)
                val pathDB = createPathDB(path, Constants.FIREBASE_LEVEL_CHANGE)
                workInterface.pathUpload(pathDB, userId, appContext, false)
                operationSuccess()
            }
            catch (exception: Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

        //Delete a path after its concluded (max level successfully completed)
    fun pathMaxLevelUpdate(path: Path, operationSuccess: () -> Unit, operationFailure: () -> Unit) {
        viewModelScope.launch {
            try{
                val userId = userDao.getUser().userId
                resultDao.deletePathResult(path.id)
                pathDao.deletePath(path)
                val pathDB = createPathDB(path, Constants.FIREBASE_PATH_END)
                workInterface.pathUpload(pathDB, userId, appContext, false)
                operationSuccess()
            }
            catch (exception: Exception){
                operationFailure()
            }
        }

    }

        //Save the results of the concluded intervention, update its path
    fun setInt2Result(
            res1: Boolean,
            res2: Boolean,
            res3: Boolean,
            sec: Int,
            duration: Int,
            operationSuccess: () -> Unit,
            operationFailure: () -> Unit,
            calledFromService: Boolean = false
        ) {

        var path = selectedPath.value

        if (path!= null) {
            val list = path.appList.map { it.appName }
            interventionResult = InterventionResult(
                path.level,
                Calendar.getInstance(),
                list.toString(),
                path.id,
                Constants.INTERVENTION_2,
                res1,
                res2,
                res3,
                sec,
                0
            )
            path.n_interventions += 1
            if(!calledFromService)
                selectedPath.value = path!!
            else{
                initPathDeferred(path)
            }
            viewModelScope.launch {
                try{
                    val userId = userDao.getUser().userId
                    pathDao.updatePath(path)
                    // stop uploading "INTERVENTION_DONE" in paths ?
                    val pathDB = createPathDB(path, Constants.FIREBASE_INTERVENTION)
                    workInterface.pathUpload(pathDB, userId, appContext, false)
                    updatePathList()

                    val iid = resultDao.addResult(interventionResult)
                    interventionResult.id = iid
                    val interventionResultDB = createInterventionResultDB(interventionResult, duration.toLong())
                    workInterface.interventionUpload(
                        interventionResultDB,
                        userId,
                        appContext,
                        false
                    )
                    operationSuccess()
                }
                catch (exception: Exception){
                    Log.v("DB_FAILURE", exception.toString())
                    operationFailure()
                }
            }
        }
    }

        //Save the just started intervention as active
    fun addActiveIntervention(
            path: Path,
            type: Int,
            operationSuccess: () -> Unit,
            operationFailure: () -> Unit
        ) {
        viewModelScope.launch {
            try {
                val int = ActiveIntervention(path.id, type)
                activeInterventionDao.addActiveIntervention(int)
                updateActiveIntList()
                operationSuccess()
            }
            catch (exception: java.lang.Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

        //Remove the just concluded intervention from the active ones
    fun removeActiveIntervention(
            path: Path,
            operationSuccess: () -> Unit,
            operationFailure: () -> Unit
        ) {
        viewModelScope.launch {
            try {
                activeInterventionDao.removeActiveIntervention(path.id)
                updateActiveIntList()
                operationSuccess()
            }
            catch (exception: java.lang.Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

        //Update the active intervention, setting the pause flag
    fun pauseActiveIntervention(
            path: Path,
            type: Int,
            operationSuccess: () -> Unit,
            operationFailure: () -> Unit
        ) {
        viewModelScope.launch {
            try {
                val int = ActiveIntervention(path.id, type, true)
                activeInterventionDao.updateActiveIntervention(int)
                updateActiveIntList()
                operationSuccess()
            }
            catch (exception: java.lang.Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

    //Update the active intervention, setting the pause flag
    fun unpauseActiveIntervention(
        path: Path,
        type: Int,
        operationSuccess: () -> Unit,
        operationFailure: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val int = ActiveIntervention(path.id, type, false)
                activeInterventionDao.updateActiveIntervention(int)
                updateActiveIntList()
                operationSuccess()
            }
            catch (exception: java.lang.Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

        //Add the selected app to the excluded ones (the ones for which no suggestions of starting an path are done)
    fun addExcludedApp(app: App, operationSuccess: () -> Unit, operationFailure: () -> Unit) {
        viewModelScope.launch {
            try{
                excludedAppDao.addExcludedApp(app)
                operationSuccess()
            }
            catch (exception: java.lang.Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

        //check if an app is in the excluded app group
    fun getExcludedApp(appPackage: String): LiveData<App> {
        return excludedAppDao.getExcludedApp(appPackage)
    }

    fun removeExcludedApp(
        appName: String,
        appPackage: String,
        operationSuccess: () -> Unit,
        operationFailure: () -> Unit
    ) {
        viewModelScope.launch {
            val app = App(appName, appPackage)
            try{
                excludedAppDao.deleteExcludedApp(app)
                operationSuccess()
            }
            catch (exception: java.lang.Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }

        //Mark a notification as opened so that it won't appear again in the list (but its data is maintained)
    fun updateNotificationOpened(
            usage: BadUsageNotification,
            operationSuccess: () -> Unit,
            operationFailure: () -> Unit
        ){
        usage.opened = true
        viewModelScope.launch {
            try{
                badUsageDao.updateUsage(usage)
                updateNotificationList()
                operationSuccess()
            }
            catch (exception: Exception){
                Log.v("DB_FAILURE", exception.toString())
                operationFailure()
            }
        }
    }



    fun initPathForIntervention(pathId: Long) {
        viewModelScope. launch {
            selectedPath.value = pathDao.getPathForIntervention(pathId)
        }
    }

    fun removeAllActiveInterventions() {
        viewModelScope.launch {
            activeInterventionDao.removeAllActiveIntervention()
        }
    }



    private fun createPathDB(path: Path, opType: String): PathDB {
        val pathDate = functions.getDateStr(path.start_date.time)
        val opDate = functions.getDateTimeStr(Calendar.getInstance().time)
        return PathDB(
            path.id,
            path.intervention,
            path.level,
            path.appList.toString(),
            pathDate,
            path.n_interventions,
            path.n_miss_interventions,
            path.int_duration,
            path.appUsageList.toString(),
            opDate,
            opType,
            path.hasVibration,
            path.hasDisplayModification
        )
    }

    private fun createInterventionResultDB(result: InterventionResult, duration:Long): InterventionResultDB {
        val opDate = functions.getDateTimeStr(result.date.time)
        return InterventionResultDB(
            result.id,
            result.path_id,
            result.level,
            opDate,
            result.apps,
            result.type,
            result.start_pen,
            result.end_pen,
            result.del_pen,
            result.sec_delay,
            result.n_openings,
            duration
        )
    }

    fun addMessage(message: MessageClass) {
        messageList.add(message)
        uploadChat()
    }

    fun updateNPathChatbot(n : Int){
        nPathChatbot = n
        uploadChat()
    }

    fun initStats(context: Context){
        viewModelScope.launch {
            val lastWeekList = statManager.getPhoneUsage(context, functions.getMsec7DaysAgo(), System.currentTimeMillis())
            val list = statManager.getPhoneUsage(context, functions.getMsec24HoursAgo(), System.currentTimeMillis())
            val todayList = statManager.getPhoneUsage(context, functions.getMsecStartOfDay(), System.currentTimeMillis())

            lastWeekUsage.postValue(lastWeekList)
            statList.postValue(list)
            startOfDayStatList.postValue(todayList)

            dangerousApps.removeAll{true}
            list.forEach {
                if (functions.dangerousApp(it , lastWeekList))
                    dangerousApps.add(
                        functions.mapToSuggestion(it)
                    )
            }
        }
    }

    private fun createChatDB(): ChatDB {
        val chatDate = functions.getDateTimeSecStr(messageList[0].date.time)
        val duration = functions.getChatDuration(messageList)
        return ChatDB(
            chatDate,
            messageList.map{it.message}.toList(),
            duration,
            nPathChatbot
        )
    }

    fun initConversation() {
        messageList.removeAll { true }
        nPathChatbot = 0
    }

    private fun uploadChat(){
        viewModelScope.launch {
            try{
                val userId = userDao.getUser().userId
                val chatDB = createChatDB()
                workInterface.chatUpload(chatDB, userId, appContext, false)
            }
            catch (exception: Exception){
                Log.v("DB_FAILURE", exception.toString())
            }
        }
    }

    fun initPermanentNotification(applicationContext: Context) {
        Log.d("SERVICE2", "INIT NOTIFICATION")
        if(!::notification.isInitialized) {
            val pendingIntent = NavDeepLinkBuilder(applicationContext)
                .setComponentName(HomeActivity::class.java)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(R.id.nav_home_paths)
                .createPendingIntent()
            notification =
                NotificationCompat.Builder(applicationContext, Constants.FOREGROUND_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle("StepByStep running")
                    .setContentIntent(pendingIntent)
                    .build()
        }
    }

}