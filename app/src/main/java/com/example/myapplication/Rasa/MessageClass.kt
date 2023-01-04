package com.example.myapplication.Rasa

import com.example.myapplication.db.data.SuggestionOnApp
import java.util.*

data class MessageClass(var message:String="",var sender:Int=0, var dangerousApps: MutableList<SuggestionOnApp> = mutableListOf<SuggestionOnApp>() ) {
    val date: Calendar = Calendar.getInstance()
}