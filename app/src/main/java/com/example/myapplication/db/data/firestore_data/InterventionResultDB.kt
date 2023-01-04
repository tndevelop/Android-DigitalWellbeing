package com.example.myapplication.db.data.firestore_data

data class InterventionResultDB(
        val id: Long,
        val pathId: Long,
        val level: Int,
        val date: String,
        val appList: String,
        val type: Int,
        val int2_start_pen: Boolean,
        val int2_end_pen: Boolean,
        val int2_del_pen: Boolean,
        val int2_sec_delay: Int,
        val int1_n_openings: Int,
        val duration: Long
)
