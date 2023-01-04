package com.example.myapplication.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.Rasa.MessageClass
import com.example.myapplication.activities.DummyBrightnessActivity
import com.example.myapplication.activities.PopUpWindow
import com.example.myapplication.db.data.App
import com.example.myapplication.db.data.CustomUsageStats
import com.example.myapplication.db.data.Path
import com.example.myapplication.db.data.SuggestionOnApp
import com.example.myapplication.viewModels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToLong

class Functions {

    /*
        Return true if the path given have a number of interventions higher than a threshold (constant) and a certain number of day (constant) have passed since its start
        Used to determine if a recap for leveling up have to be called on this path
     */
    fun checkRecap(path: Path) :Boolean{
        var result = (path.n_interventions >= Constants.N_INTERVENTIONS_P2_LEVEL_UP && ((Calendar.getInstance().timeInMillis - path.start_date.timeInMillis) / Constants.DAYS_TO_MSEC) >= Constants.N_DAYS_LEVEL_UP)
        return result
    }


    fun hideKeyBoard(context: Context, view: View?) {
        val inputManager: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }



    fun isMonitoringPhase(date: Calendar): Boolean{
        return daysDifference(date) < Constants.N_DAYS_MONITORING_PHASE
    }
    fun createPath2(path: Path, viewModel: HomeViewModel) {
        /*if(existingInt2)
            openP2Dialog(false)
        else*/
        viewModel.upgradePathFWto2(path, null, { operationSuccess() }, { operationFailure() })
    }


    fun daysDifference(date: Calendar): Double {
        val now = Calendar.getInstance()
        return ((now.timeInMillis - date.timeInMillis).toDouble() / Constants.DAYS_TO_MSEC)
    }

    fun convertMsecToHHMM(mSec: Long): String {
        val hours = mSec / Constants.HOUR_TO_MSEC
        val minutes = (mSec % Constants.HOUR_TO_MSEC) / Constants.MIN_TO_MSEC
        val hStr = if(hours<10) "0$hours" else hours.toString()
        val mStr = if(minutes<10) "0$minutes" else minutes.toString()
        return "$hStr:$mStr"
    }

    fun getMsecStartOfDay(): Long {
        val day = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND,0)
        }
        return day.timeInMillis
    }

    fun getMsec7DaysAgo(): Long {

        val day = Calendar.getInstance().apply {
            add(Calendar.DATE,  -7)
        }
        return day.timeInMillis
    }

    fun getMsec24HoursAgo(): Long {

        val day = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY,  -24)
        }
        return day.timeInMillis
    }

    fun inNDays(calendar: Calendar, n: Double): Calendar {
        var day = calendar.clone() as Calendar
        if(n > 1) {
            day.apply {
                add(Calendar.DATE, n.toInt())
            }

        }else{
            day.apply {
                add(Calendar.MILLISECOND, (n * Constants.DAYS_TO_MSEC).toInt())
            }

        }
        return day
    }

    fun createPopUp(context : Context, title : String, message: String) {
        val intent = Intent(context, PopUpWindow::class.java)
        intent.putExtra("popupTitle", title)
        intent.putExtra("popupText", message)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        Log.v("SERVICE2", "SERVICE_TIMER_DIALOG")
        startActivity(context, intent, bundleOf())
    }

    fun getDateTimeStr(time: Date): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ITALIAN)
        return dateFormat.format(time)
    }

    fun getDateTimeSecStr(time: Date): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALIAN)
        return dateFormat.format(time)
    }

    fun getDateStr(time: Date): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ITALIAN)
        return dateFormat.format(time)
    }

     fun changeScreenBrightness(context: Context, screenBrightnessValue: Int) {
        // Check whether has the write settings permission or not.
        val settingsCanWrite = hasWriteSettingsPermission(context)

        // If do not have then open the Can modify system settings panel.
        if (!settingsCanWrite) {
            changeWriteSettingsPermission(context)
        }else {

            // Change the screen brightness change mode to manual.
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            // Apply the screen brightness value to the system, this will change
            // the value in Settings ---> Display ---> Brightness level.
            // It will also change the screen brightness for the device.
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, screenBrightnessValue
            )

            // Apply brightness by creating a dummy activity
            val intent = Intent(context, DummyBrightnessActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("brightness value", screenBrightnessValue)
            startActivity(context, intent, bundleOf())
        }
    }

    fun getScreenBrightness(context: Context) : Int {

        val brightness = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        )

        return brightness

    }

    // Check whether this app has android write settings permission.
    @RequiresApi(Build.VERSION_CODES.M)
     fun hasWriteSettingsPermission(context: Context): Boolean {
        var ret = true
        // Get the result from below code.
        ret = Settings.System.canWrite(context)
        return ret
    }

     fun changeWriteSettingsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent)
    }

    fun requiresGreyOutOrVibration(app: CustomUsageStats): Int {
        if (requiresGreyOut(app))
            return 0
        else return 1
    }

    fun hasBrightnessOrVibration(app: SuggestionOnApp): Int{
        if (app.greyOutSuggested && app.vibrationSuggested)
            return 2
        else if(app.vibrationSuggested)
            return 1
        else
            return 0
    }

    fun requiresGreyOut(app: CustomUsageStats): Boolean {
        return  listOf(AppCategory.CATEGORY_GAME.toString(), AppCategory.CATEGORY_IMAGE.toString(), AppCategory.CATEGORY_SOCIAL.toString(), AppCategory.CATEGORY_VIDEO.toString(), AppCategory.CATEGORY_UNDEFINED.toString())
            .contains(app.category) && !Constants.VIBRATION_APPS.contains(app.appName)

    }

    fun requiresVibration(app: CustomUsageStats): Boolean {

        return  listOf(AppCategory.CATEGORY_SOCIAL.toString()).contains(app.category)
                ||   Constants.VIBRATION_APPS.contains(app.appName)

    }

    fun dangerousCategory(app: CustomUsageStats): Boolean {
        val isDangerous =  listOf(AppCategory.CATEGORY_GAME.toString(), AppCategory.CATEGORY_IMAGE.toString(), AppCategory.CATEGORY_VIDEO.toString(), AppCategory.CATEGORY_SOCIAL.toString(), AppCategory.CATEGORY_UNDEFINED.toString())
            .contains(app.category)
        return isDangerous
    }

    fun getChatDuration(messageList: MutableList<MessageClass>): Long {
        val firstMessage = messageList.get(0)
        val lastMessage = messageList.get(messageList.size-1)
        return lastMessage.date.timeInMillis - firstMessage.date.timeInMillis
    }

    private fun operationSuccess(){

        Log.d("TOAST", "Operation successful")

    }

    private fun operationFailure() {

        Log.d("TOAST", "Database error")
    }

    fun dangerousApp(app: CustomUsageStats, lastWeekUsage: List<CustomUsageStats> = listOf()): Boolean {
        val avgUsage: Long
        if(lastWeekUsage.size > 0) {
            val thisApp = lastWeekUsage.filter { it.appName == app.appName }[0]
            avgUsage = thisApp.timeInForeground / 7
            Log.d("FUNCTIONS_DEBUG", "${app.appName} ${app.timeInForeground} > ${avgUsage * Constants.PERCENTAGE_MORE_THAN_AVERAGE} ?")
        }else{
            //if comparison with last week is disabled last condition will be (timeInForeground > 0) therefore always true
            avgUsage = 0
        }

        return app.timeInForeground > Constants.FOREGROUND_LIMIT_DANGEROUS_APP && dangerousCategory(app)
                && app.timeInForeground > avgUsage * Constants.PERCENTAGE_MORE_THAN_AVERAGE
    }

    //gets the MAX_APPS_SUGGESTED most used apps, among those without a path started already
    fun getSuggestionApps(dangerousApps: MutableList<SuggestionOnApp>, viewModel: HomeViewModel, allApps: List<CustomUsageStats>): MutableList<SuggestionOnApp> {
        return dangerousApps.filter {
            val dang = it;
            viewModel.getPathList().value?.filter {
                it.appList.map { it.appName }.contains(dang.appName)
            }?.size == 0
        }.sortedBy {
            val dang =
                it; allApps.filter { it.appName == dang.appName }[0].timeInForeground
        }.asReversed()
            .take(Constants.MAX_APPS_SUGGESTED).toMutableList()

    }

    fun createPaths(selectedApps: MutableList<SuggestionOnApp>, viewModel: HomeViewModel) {
        for (app in selectedApps) {
            if(app.appPackage != null && app.appPackage != "") {
                viewModel.addNewPath(
                    App(app.appName, app.appPackage),
                    { operationSuccess() },
                    { operationFailure() },
                    app
                )
                viewModel.updateNPathChatbot(viewModel.nPathChatbot + 1)
            }
        }
    }

     fun messageReplacement(text: String, suggestionPool: MutableList<SuggestionOnApp>, allApps: List<CustomUsageStats>, selectedApp: SuggestionOnApp?, appName : String? = null): String {
        var newText = text
         // workaround suggestionPool not yet available in proactivity and manual
        if(appName != null)
            newText = newText.replace("\$app", appName)
        if(text.contains("\$") && suggestionPool.size > 0) {
            newText = newText.replace("\$minutes", (allApps.filter{it.appName == suggestionPool[0].appName}[0].timeInForeground / Constants.MIN_TO_MSEC).toString())
            newText = newText.replace("\$app", suggestionPool[0].appName)
            newText = newText.replace("\$times", allApps.filter{it.appName == suggestionPool[0].appName}[0].nOpenings.toString())
            newText = newText.replace("\$time", suggestionPool[0].suggestedTime.toString())
            newText = newText.replace("\$npaths", Integer.min(suggestionPool.size, Constants.MAX_APPS_SUGGESTED)
                .toString())
            newText = newText.replace("\$pathpaths", Constants.pathOrPaths.get(Integer.min(suggestionPool.size - 1, Constants.PLURAL - 1)))
            newText = newText.replace("\$hashave", Constants.hasOrHave[Integer.min(suggestionPool.size - 1, Constants.PLURAL - 1)])
            newText = newText.replace("\$itthem", Constants.itOrThem[Integer.min(suggestionPool.size - 1, Constants.PLURAL - 1)])
            newText = newText.replace("\$use", Constants.usesOrNot[requiresGreyOutOrVibration(allApps.filter{it.appName == suggestionPool[0].appName}[0])])
            newText = newText.replace("\$intervention", Constants.brightnessOrVibration[hasBrightnessOrVibration(suggestionPool[0])])

            selectedApp?.let {
                newText = newText.replace("\$asked_interventions", Constants.brightnessOrVibration[requiresGreyOutOrVibration(allApps.filter { it.appName == selectedApp!!.appName }[0])])
                newText = newText.replace("\$asked_app", selectedApp!!.appName)
                newText = newText.replace("\$asked_time", selectedApp!!.suggestedTime.toString())
                newText = newText.replace("\$asked_times", allApps.filter { it.appName == selectedApp!!.appName }[0].nOpenings.toString())
                newText = newText.replace("\$asked_minutes", allApps.filter { it.appName == selectedApp!!.appName }[0].timeInForeground.toString())
            }

        }
        return newText
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun selectApp(suggestionPool: MutableList<SuggestionOnApp>, userMessage: String, receivedSelectedApp: SuggestionOnApp?): SuggestionOnApp? {
        var selectedApp = receivedSelectedApp
        for(app in suggestionPool){
            if(userMessage.lowercase().contains(app.appName.lowercase())) {
                selectedApp = app
            }
            if(selectedApp == null){ //if user didn't specify the app he's asking about, we'll just talk about the first one
                selectedApp = suggestionPool[0]
            }
        }
        return selectedApp
    }

    fun mapToSuggestion(it: CustomUsageStats): SuggestionOnApp {
        return SuggestionOnApp(
            it.appName,
            it.appPackage,
            suggestTime(it),
            requiresVibration(it),
            requiresGreyOut(it)
        )
    }

    private fun suggestTime(it: CustomUsageStats): Long {
        if(it.nOpenings > 0)
            return java.lang.Long.max(ceil(20.0 / it.nOpenings.toDouble()).roundToLong(), (ceil(it.timeInForeground.toDouble() / it.nOpenings.toDouble()).roundToLong() / (60 * 1000)))
        else
            return 1L
    }

    fun setUpChat(comesFrom: String, requireArguments: Bundle?, dangerousApps: MutableList<SuggestionOnApp>, viewModel: HomeViewModel, allApps: List<CustomUsageStats>, cases: List<String>): MutableList<SuggestionOnApp> {
        var suggestionPool = mutableListOf<SuggestionOnApp>()
        when (comesFrom) {
            cases[0] -> {//eow
                suggestionPool = getSuggestionApps(dangerousApps, viewModel, allApps)
            }
            cases[1] -> {//manual
                suggestionPool = populatePoolWithRequestedApp(requireArguments!!, allApps)
            }
            cases[2] -> {//proactivity
                suggestionPool = populatePoolWithRequestedApp(requireArguments!!, allApps)
            }
            cases[3] -> {//save
                suggestionPool = requireArguments!!.get("apps") as MutableList<SuggestionOnApp>
                //selectedApp = suggestionPool[0]
            }
            cases[4] -> {//modify_complete
                val inputApps = requireArguments!!.get("apps") as MutableList<SuggestionOnApp>
                val selectedFlags = requireArguments.get("includedApps") as MutableList<Boolean>
                var filteredApps = mutableListOf<SuggestionOnApp>()
                for (i in 0..inputApps.size - 1) {
                    if (selectedFlags[i])
                        filteredApps.add(inputApps[i])
                }
                suggestionPool = filteredApps
            }
        }
        return suggestionPool
    }

    private fun populatePoolWithRequestedApp(requireArguments: Bundle, allApps: List<CustomUsageStats>): MutableList<SuggestionOnApp> {
        var suggestionPool = mutableListOf<SuggestionOnApp>()
        val appPackage = requireArguments.getString("appPackage")
        val app = allApps.filter{ it.appPackage == appPackage}
        suggestionPool = app.map{
            mapToSuggestion(it)
        }.toMutableList()
        return suggestionPool
    }


}