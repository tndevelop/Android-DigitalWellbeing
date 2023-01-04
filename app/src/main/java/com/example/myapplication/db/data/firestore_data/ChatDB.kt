package com.example.myapplication.db.data.firestore_data

data class ChatDB(val date: String, val messages: List<String>, val durationMilliseconds: Long, val pathsCreated: Int)
