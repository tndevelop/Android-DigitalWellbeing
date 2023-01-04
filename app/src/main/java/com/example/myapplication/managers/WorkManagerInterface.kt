package com.example.myapplication.managers

import android.content.Context
import androidx.work.*
import com.example.myapplication.db.data.firestore_data.ChatDB
import com.example.myapplication.db.data.firestore_data.InterventionResultDB
import com.example.myapplication.db.data.firestore_data.PathDB
import com.example.myapplication.db.data.firestore_data.UsageStatDB
import com.example.myapplication.utils.Constants
import java.util.concurrent.TimeUnit

class WorkManagerInterface {


    fun pathUpload(path: PathDB, uid: String, context: Context, delay: Boolean) {

        val data: Data = workDataOf(
                "UPLOAD_TYPE" to Constants.PATH_UPLOAD,
                "USER_ID" to uid,
                "PATH_ID" to path.id,
                "PATH_INTERVENTION" to path.intervention,
                "PATH_LEVEL" to path.level,
                "PATH_APP_LIST" to path.appList,
                "PATH_DATE" to path.start_date,
                "PATH_N_INTERVENTIONS" to path.n_interventions,
                "PATH_N_MISS_INTERVENTIONS" to path.n_miss_interventions,
                "OPERATION_DATE" to path.operationDate,
                "OPERATION_TYPE" to path.operationType,
                "HAS_VIBRATION" to path.hasVibration,
                "HAS_DISPLAY_MODIFICATION" to path.hasDisplayModification
        )

        val workRequest: WorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(data)
                .setInitialDelay(if (delay) 10 else 0, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun interventionUpload(result: InterventionResultDB, uid: String, context: Context, delay: Boolean) {
        val data: Data = workDataOf(
                "UPLOAD_TYPE" to Constants.INTERVENTION_RESULT_UPLOAD,
                "USER_ID" to uid,
                "INT_ID" to result.id,
                "PATH_ID" to result.pathId,
                "INT_LEVEL" to result.level,
                "INT_DATE" to result.date,
                "INT_APP" to result.appList,
                "INT_TYPE" to result.type,
                "INT2_PEN1" to result.int2_start_pen,
                "INT2_PEN2" to result.int2_end_pen,
                "INT2_PEN3" to result.int2_del_pen,
                "INT2_SEC" to result.int2_sec_delay,
                "INT1_NOP" to result.int1_n_openings,
                "DURATION" to result.duration
        )

        val workRequest: WorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(data)
                .setInitialDelay(if (delay) 10 else 0, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun usageUpload(usage: UsageStatDB, uid: String, context:Context, delay: Boolean) {
        val data: Data = workDataOf(
                "UPLOAD_TYPE" to Constants.USAGE_UPLOAD,
                "USER_ID" to uid,
                "PATH_DATE" to usage.date,
                "APP_NAME" to usage.appName,
                "APP_PACKAGE" to usage.appPackage,
                "TIME_FOREGROUND" to usage.timeInForeground,
                "N_OPENINGS" to usage.nOpenings,
                "START" to usage.start,
                "END" to usage.end
        )

        val workRequest: WorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(data)
                .setInitialDelay(if (delay) 10 else 0, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun chatUpload(chat: ChatDB, uid: String, context: Context, delay: Boolean) {
        val data: Data = workDataOf(
            "UPLOAD_TYPE" to Constants.CHAT_UPLOAD,
            "USER_ID" to uid,
            "CONVERSATION_DATE" to chat.date,
            "MESSAGES" to chat.messages.stream().toArray{arrayOfNulls<String>(it)},
            "DURATION" to chat.durationMilliseconds,
            "N_PATHS" to chat.pathsCreated
        )

        val workRequest: WorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(data)
            .setInitialDelay(if (delay) 10 else 0, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)


    }
}