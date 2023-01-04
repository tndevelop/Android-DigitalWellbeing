package com.example.myapplication.managers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.db.data.firestore_data.ChatDB
import com.example.myapplication.db.data.firestore_data.InterventionResultDB
import com.example.myapplication.db.data.firestore_data.PathDB
import com.example.myapplication.db.data.firestore_data.UsageStatDB
import com.example.myapplication.utils.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UploadWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams){

    inner class FirebaseDataExample(val example: Int)

    val db = Firebase.firestore
    private val workInterface = WorkManagerInterface()
    private val context = appContext
    private val example = FirebaseDataExample(0)



    override fun doWork(): Result {

        when (inputData.getInt("UPLOAD_TYPE", 0)) {

            Constants.PATH_UPLOAD -> {
                val uid = inputData.getString("USER_ID")
                val appList = inputData.getString("PATH_APP_LIST")
                val startDate = inputData.getString("PATH_DATE")
                val opDate = inputData.getString("OPERATION_DATE")
                val opType = inputData.getString("OPERATION_TYPE")

                if (uid == null || appList == null || startDate == null || opDate == null || opType == null)
                    return Result.failure()
                //TODO set right int_duration and appUsageList
                val pathDB = PathDB(
                        inputData.getLong("PATH_ID", -1),
                        inputData.getInt("PATH_INTERVENTION", -1),
                        inputData.getInt("PATH_LEVEL", -1),
                        appList,
                        startDate,
                        inputData.getInt("PATH_N_INTERVENTIONS", -1),
                        inputData.getInt("PATH_N_MISS_INTERVENTIONS", -1),
                        0,
                        "",
                        opDate,
                        opType,
                        inputData.getBoolean("HAS_VIBRATION", false),
                        inputData.getBoolean("HAS_DISPLAY_MODIFICATION", false)
                )

                db.collection("users").document(uid).set(example)
                db.collection("users").document(uid).collection("paths").document().set(pathDB)
                        .addOnFailureListener {
                            Log.v("THESIS-APP", "FIRESTORE_ERROR:: $it")
                            //re-execute the work request after an initial delay
                            workInterface.pathUpload(pathDB, uid, context, true)
                        }
            }


            Constants.INTERVENTION_RESULT_UPLOAD -> {
                val uid = inputData.getString("USER_ID")
                val date = inputData.getString("INT_DATE")
                val appList = inputData.getString("INT_APP")

                if (uid == null || date == null || appList == null)
                    return Result.failure()

                val interventionResultDB = InterventionResultDB(
                        inputData.getLong("INT_ID", -1),
                        inputData.getLong("PATH_ID", -1),
                        inputData.getInt("INT_LEVEL", -1),
                        date,
                        appList,
                        inputData.getInt("INT_TYPE", -1),
                        inputData.getBoolean("INT2_PEN1", false),
                        inputData.getBoolean("INT2_PEN2", false),
                        inputData.getBoolean("INT2_PEN3", false),
                        inputData.getInt("INT2_SEC", -1),
                        inputData.getInt("INT1_NOP", -1),
                        inputData.getLong("DURATION", -1)
                )

                db.collection("users").document(uid).set(example)
                db.collection("users").document(uid).collection("interventions").document().set(interventionResultDB)
                        .addOnFailureListener {
                            Log.v("THESIS-APP", "FIRESTORE_ERROR:: $it")
                            //re-execute the work request after an initial delay
                            workInterface.interventionUpload(interventionResultDB, uid, context, true)
                        }
            }


            Constants.USAGE_UPLOAD -> {
                val uid = inputData.getString("USER_ID")
                val date = inputData.getString("PATH_DATE")
                val appName = inputData.getString("APP_NAME")
                val appPackage = inputData.getString("APP_PACKAGE")

                if(uid == null || appName == null || appPackage == null || date == null)
                    return Result.failure()

                val usage = UsageStatDB(appName, appPackage, date,
                        inputData.getLong("TIME_FOREGROUND", -1),
                        inputData.getInt("N_OPENINGS", -1),
                        inputData.getLong("START", -1),
                        inputData.getLong("END", -1)
                )

                db.collection("users").document(uid).set(example)
                db.collection("users").document(uid).collection("usage").document(date).set(example)
                db.collection("users").document(uid).collection("usage").document(date).collection("usageData").document(appPackage).set(usage)
                        .addOnFailureListener {
                            Log.v("THESIS-APP", "FIRESTORE_ERROR:: $it")
                            //re-execute the work request after an initial delay
                            workInterface.usageUpload(usage, uid, context, true)
                        }
            }

            Constants.CHAT_UPLOAD -> {
                val uid = inputData.getString("USER_ID")
                val date = inputData.getString("CONVERSATION_DATE")
                val messages = inputData.getStringArray("MESSAGES")
                val durationMilliseconds = inputData.getLong("DURATION", -1L)
                val pathsCreated = inputData.getInt("N_PATHS", -1)

                if(uid == null || date == null || messages == null || durationMilliseconds == -1L || pathsCreated == -1)
                    return Result.failure()

                val chat = ChatDB(
                    date,
                    messages.toList(),
                    durationMilliseconds,
                    pathsCreated
                )

                db.collection("users").document(uid).set(example)
                db.collection("users").document(uid).collection("chats").document(date).set(chat)

                    .addOnFailureListener {
                        Log.v("THESIS-APP", "FIRESTORE_ERROR:: $it")
                        //re-execute the work request after an initial delay
                        workInterface.chatUpload(chat, uid, context, true)
                    }
            }

            else -> {
                return Result.failure()
            }
        }
        return Result.success()
    }

}