package com.example.myapplication.db.data

import java.io.Serializable

data class SuggestionOnApp(
    val appName: String = "",
    var appPackage: String = "",
    var suggestedTime: Long = 0,
    var vibrationSuggested: Boolean = false,
    var greyOutSuggested: Boolean = false
) : Serializable{
}