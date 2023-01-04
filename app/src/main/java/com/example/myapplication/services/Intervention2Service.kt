package com.example.myapplication.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.example.myapplication.R
import com.example.myapplication.activities.HomeActivity
import com.example.myapplication.activities.PopUpWindow
import com.example.myapplication.db.data.App
import com.example.myapplication.managers.UsageStatManager
import com.example.myapplication.utils.Constants
import com.example.myapplication.utils.Functions
import com.example.myapplication.viewModels.HomeViewModel

class Intervention2Service : Service() {

    private val myBinder = LocalBinder()
    private lateinit var context: Context
    private val statManager = UsageStatManager()
    private val functions = Functions()

    private lateinit var timer: CountDownTimer
    private lateinit var secondTimer: CountDownTimer
    private var secondsDelay: Int = 0
    private var popupCounter = 0

    private var pathId = 0L
    private lateinit var app: App
    private var startTimestamp = 0L

    private var duration = 0
    private var startPenalty = false
    private var endPenalty = false
    private var worstPenalty = false
    private var secondsPenalty = 0
    inner class Result(val startPenalty: Boolean, var endPenalty: Boolean, val delayPenalty: Boolean, val secondsDelay: Int, val duration: Int)
    private lateinit var viewModel: HomeViewModel

    override fun onCreate() {
        Log.v("SERVICE2", "SERVICE_CREATED")
        super.onCreate()
        viewModel = HomeViewModel(applicationContext as Application)
        context = applicationContext
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.v("SERVICE2", "SERVICE_BOUND")
        return myBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.v("SERVICE2", "SERVICE_DESTROYED")
        super.onDestroy()
    }

    fun startTimer(appN: App, durationN: Int, level: Int, penalty: Boolean, id: Long, hasVibration: Boolean, hasDisplayModification: Boolean){
        Log.v("SERVICE2", "TIMER_START: $durationN $level")
        pathId = id
        app = appN
        duration = durationN
        startTimestamp = System.currentTimeMillis()
        startPenalty = penalty

        buildTimersOnLevel(app.appName, duration, level, hasVibration, hasDisplayModification)
        timer.start()

        startNotification()
    }

    fun stopTimer(): Result {
        if(::timer.isInitialized) {
            timer.cancel()
            secondTimer.cancel()

            val realTime = /*statManager.getAppUsage(
                applicationContext,
                app.appPackage,
                startTimestamp,
                System.currentTimeMillis()
            ).timeInForeground*/ System.currentTimeMillis() - startTimestamp
            Log.d("SERVICE2 start ", startTimestamp.toString())
            Log.d("SERVICE2 end ", System.currentTimeMillis().toString())
            Log.d("SERVICE2 real ", realTime.toString())
            val wantedTime = (duration * Constants.MIN_TO_MSEC).toLong()
            Log.d("SERVICE2 wanted ", wantedTime.toString())
            Log.d("SERVICE2 endPenalty ", endPenalty.toString())
            Log.d("SERVICE2 worstPenalty ", worstPenalty.toString())
            if (realTime > wantedTime) {
                if (realTime > (wantedTime * Constants.MAX_DELAY_INT2)) {
                    endPenalty = true
                    worstPenalty = true
                    secondsPenalty = 180
                } else {
                    endPenalty = true
                    worstPenalty = false
                    secondsPenalty = ((realTime - wantedTime) / Constants.SEC_TO_MSEC).toInt()
                }
            }

            Log.v("SERVICE2", "TIMER_STOP: $startPenalty $endPenalty $worstPenalty $secondsDelay")
            return Result(false, endPenalty, worstPenalty, secondsDelay, duration)
        }
        return Result(false, true, true, 60, 60)
    }

    fun destroyNotification() {
        stopForeground(true)
        stopSelf()
    }


    inner class LocalBinder : Binder() {
        fun getService() : Intervention2Service {
            return this@Intervention2Service
        }
    }


    private fun buildTimersOnLevel(
        appName: String,
        duration: Int,
        level: Int,
        hasVibration: Boolean,
        hasDisplayModification: Boolean
    ) {
        popupCounter = duration
        var vibrations = 0
        var brightnessInts = 0
        when (level) {
            1 -> {
                secondTimer = object : CountDownTimer((duration * Constants.MIN_TO_MSEC).toLong(), Constants.SEC_TO_MSEC.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {
                        secondsDelay++

                        vibrations = vibrateIfNeeded(hasVibration, millisUntilFinished, duration, vibrations, Constants.PERCENTAGES_L1_VIBRATION, timeExceeded=true)
                        brightnessInts = unbrightIfNeeded(hasDisplayModification, millisUntilFinished, duration, brightnessInts, Constants.PERCENTAGES_L1_BRIGHTNESS, timeExceeded=true )
                        /*
                        if ((secondsDelay % 60) == 0) {
                            //interventionAction(hasVibration, hasDisplayModification)
                        }
                        if (secondsDelay == 60) {
                            createPopUp(getString(R.string.popup2_int2, appName))
                        }*/
                    }

                    override fun onFinish() {
                        interventionAction(hasVibration, hasDisplayModification)
                        Log.v("SERVICE2", "SERVICE_TIMER2_END")
                        //createPopUp(getString(R.string.popup3_int2, appName))
                    }
                }
                timer = object : CountDownTimer((duration * Constants.MIN_TO_MSEC).toLong(), Constants.SEC_TO_MSEC.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {

                        vibrations = vibrateIfNeeded(hasVibration, millisUntilFinished, duration, vibrations, Constants.PERCENTAGES_L1_VIBRATION)
                        brightnessInts = unbrightIfNeeded(hasDisplayModification, millisUntilFinished, duration, brightnessInts, Constants.PERCENTAGES_L1_BRIGHTNESS )

                        /*
                        if (popupCounter == 1) {
                            createPopUp(getString(R.string.popup1_int2))
                        }
                        Log.v("SERVICE2", "TICK: $popupCounter")
                        popupCounter -= 1*/
                    }

                    override fun onFinish() {
                        vibrations = vibrateIfNeeded(hasVibration, 0, duration, vibrations, Constants.PERCENTAGES_L1_VIBRATION)
                        brightnessInts = unbrightIfNeeded(hasDisplayModification, 0, duration, brightnessInts, Constants.PERCENTAGES_L1_BRIGHTNESS )


                        Log.v("SERVICE2", "SERVICE_TIMER1_END")
                        secondTimer.start()
                    }
                }
            }
            2 -> {
                secondTimer = object : CountDownTimer((duration * Constants.MIN_TO_MSEC).toLong(), Constants.SEC_TO_MSEC.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {
                        secondsDelay++
                        vibrations = vibrateIfNeeded(hasVibration, millisUntilFinished, duration, vibrations, Constants.PERCENTAGES_L2_VIBRATION, timeExceeded=true)
                        brightnessInts = unbrightIfNeeded(hasDisplayModification, millisUntilFinished, duration, brightnessInts, Constants.PERCENTAGES_L2_BRIGHTNESS, timeExceeded=true )

                        /*

                            if ((secondsDelay % 60) == 0) {
                                interventionAction(hasVibration, hasDisplayModification)
                            }
                            if (secondsDelay == 60) {
                                createPopUp(getString(R.string.popup2_int2, appName))
                            }
                            */

                    }

                    override fun onFinish() {
                        interventionAction(hasVibration, hasDisplayModification)
                        Log.v("SERVICE2", "SERVICE_TIMER2_END")
                        //createPopUp(getString(R.string.popup3_int2, appName))
                    }
                }
                timer = object : CountDownTimer((duration * Constants.MIN_TO_MSEC).toLong(), (2 * Constants.SEC_TO_MSEC).toLong()) {
                    override fun onTick(millisUntilFinished: Long) {
                        vibrations = vibrateIfNeeded(hasVibration, millisUntilFinished, duration, vibrations, Constants.PERCENTAGES_L2_VIBRATION)
                        brightnessInts = unbrightIfNeeded(hasDisplayModification, millisUntilFinished, duration, brightnessInts, Constants.PERCENTAGES_L2_BRIGHTNESS )

                        //Log.v("SERVICE2", "TICK: $popupCounter")

                    }

                    override fun onFinish() {
                        vibrations = vibrateIfNeeded(hasVibration, 0, duration, vibrations, Constants.PERCENTAGES_L2_VIBRATION)
                        brightnessInts = unbrightIfNeeded(hasDisplayModification, 0, duration, brightnessInts, Constants.PERCENTAGES_L2_BRIGHTNESS )

                        Log.v("SERVICE2", "SERVICE_TIMER1_END")
                        secondTimer.start()
                    }
                }
            }
            3 -> {
                secondTimer = object : CountDownTimer((duration * Constants.MIN_TO_MSEC).toLong(), Constants.SEC_TO_MSEC.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {
                        secondsDelay++

                        vibrations = vibrateIfNeeded(hasVibration, millisUntilFinished, duration, vibrations, Constants.PERCENTAGES_L3_VIBRATION, timeExceeded=true)
                        brightnessInts = unbrightIfNeeded(hasDisplayModification, millisUntilFinished, duration, brightnessInts, Constants.PERCENTAGES_L3_BRIGHTNESS, timeExceeded=true )
                    }

                    override fun onFinish() {
                        //interventionAction(hasVibration, hasDisplayModification)
                        Log.v("SERVICE2", "SERVICE_TIMER2_END")
                    }
                }
                timer = object : CountDownTimer((duration * Constants.MIN_TO_MSEC).toLong(), (2 * Constants.SEC_TO_MSEC).toLong()) {
                    override fun onTick(millisUntilFinished: Long) {
                        vibrations = vibrateIfNeeded(hasVibration, millisUntilFinished, duration, vibrations, Constants.PERCENTAGES_L3_VIBRATION)
                        brightnessInts = unbrightIfNeeded(hasDisplayModification, millisUntilFinished, duration, brightnessInts, Constants.PERCENTAGES_L3_BRIGHTNESS )

                        //Log.v("SERVICE2", "TICK: $popupCounter")

                    }

                    override fun onFinish() {
                        vibrations = vibrateIfNeeded(hasVibration, 0, duration, vibrations, Constants.PERCENTAGES_L3_VIBRATION)
                        brightnessInts = unbrightIfNeeded(hasDisplayModification, 0, duration, brightnessInts, Constants.PERCENTAGES_L3_BRIGHTNESS )

                        Log.v("SERVICE2", "SERVICE_TIMER1_END")
                        secondTimer.start()
                    }
                }
            }
            4 -> {
                secondTimer = object : CountDownTimer((duration * Constants.MIN_TO_MSEC).toLong(), Constants.SEC_TO_MSEC.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {
                        secondsDelay++
                    }

                    override fun onFinish() {
                        Log.v("SERVICE2", "SERVICE_TIMER2_END")
                    }
                }
                timer = object : CountDownTimer((duration * Constants.MIN_TO_MSEC).toLong(), Constants.MIN_TO_MSEC.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        Log.v("SERVICE2", "SERVICE_TIMER1_END")
                        secondTimer.start()
                    }
                }
            }
        }
    }

    private fun unbrightIfNeeded(hasDisplayModification: Boolean, millisUntilFinished: Long, duration: Int, brightnessInts: Int, percentages: IntArray, timeExceeded: Boolean = false): Int {
        var b = brightnessInts
        if(hasDisplayModification && thisTimePercentageHasPassed(
                percentages.toList(),
                millisUntilFinished,
                duration,
                brightnessInts,
                timeExceeded
            )){
            zeroBrightness()
            b++
            Log.v("SERVICE2", "brightness " + millisUntilFinished / 1000)
        }
        return b
    }

    private fun vibrateIfNeeded(hasVibration: Boolean, millisUntilFinished: Long, duration: Int, vibrations: Int, percentages: IntArray, timeExceeded: Boolean = false): Int {
        var v = vibrations
        if(hasVibration && thisTimePercentageHasPassed(percentages.toList(), millisUntilFinished, duration, vibrations, timeExceeded) ){
            vibrate()
            v++
            Log.v("SERVICE2", "vibrate " + millisUntilFinished / 1000)
        }
        return v
    }

    private fun thisTimePercentageHasPassed(
        percentages: List<Int>,
        millisUntilFinished: Long,
        duration: Int,
        actualInterventions: Int,
        timeExceeded: Boolean
    ): Boolean {
        var percentage: Int
        for (idx in 0..percentages.size-1) {
            if(timeExceeded)
                percentage = percentages[idx] - 100
            else
                percentage= percentages[idx]
            val secondsLeft = millisUntilFinished / Constants.SEC_TO_MSEC
            val minSecondsLeftForInt = duration * 60 * (100 - percentage) / 100
            val requiredIntervention =
                secondsLeft <= minSecondsLeftForInt && actualInterventions == idx
            if(requiredIntervention) return true
        }
        return false
    }

    private fun interventionAction(hasVibration: Boolean, hasDisplayModification: Boolean) {
        if(hasVibration)
            vibrate()
        if(hasDisplayModification)
            zeroBrightness()
    }

    private fun zeroBrightness(){
        val oldBrightness = functions.getScreenBrightness(context)
        functions.changeScreenBrightness(context, 0)
        Handler(Looper.getMainLooper()).postDelayed({
            functions.changeScreenBrightness(context, oldBrightness)
        },Constants.RESTORE_BRIGHTNESS_DELAY)
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.v("SERVICE2", "SERVICE_TIMER_VIBRATION")
            //val effect = VibrationEffect.createOneShot(400, 255)
            //vibrator.vibrate(effect)
            vibrator.vibrate(VibrationEffect.createOneShot(400, 255), AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build())

            Handler(Looper.getMainLooper()).postDelayed({
                vibrator.vibrate(VibrationEffect.createOneShot(400, 255), AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build())
            }, 500 )
        } else {
            Log.v("SERVICE2", "SERVICE_TIMER_VIBRATION")
            vibrator.vibrate(400)

        }
        //Toast.makeText(context, "StepByStep Vibrating", Toast.LENGTH_LONG).show();
    }

    private fun createPopUp(message: String) {
        val intent = Intent(context, PopUpWindow::class.java)
        intent.putExtra("popupTitle", getString(R.string.intervention2))
        intent.putExtra("popupText", message)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        Log.v("SERVICE2", "SERVICE_TIMER_DIALOG")
        startActivity(intent)
    }

    private fun startNotification() {
        val channel = NotificationChannel(Constants.INTERVENTION2_NOTIFICATION_ID, getString(R.string.notification_interventions_name), NotificationManager.IMPORTANCE_LOW).apply {
            description = getString(R.string.notification_interventions_description)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        val pendingIntent = NavDeepLinkBuilder(applicationContext)
                .setComponentName(HomeActivity::class.java)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(R.id.nav_home_paths)
                .createPendingIntent()
        val notification: Notification = NotificationCompat.Builder(this, Constants.INTERVENTION2_NOTIFICATION_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(getString(R.string.notification_content_int2))
                //.setContentIntent(pendingIntent)
                .build()
        startForeground(Constants.INTERVENTION2_FOREGROUND, notification)
    }
}