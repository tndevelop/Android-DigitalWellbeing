package com.example.myapplication

import android.util.Log
import com.example.myapplication.viewModels.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUrlsService {

    suspend fun getChatbotUrl(vm: HomeViewModel) {
        val db = FirebaseFirestore.getInstance()
        var url = ""
        db.collection("URLs")
            .document("chatbot")
            .get()
            .addOnSuccessListener { document ->
                vm.chatbotBaseUrl = document["url"] as String
                Log.v("URL_SERVICE", vm.chatbotBaseUrl)
            }
            .addOnFailureListener { exception ->
                Log.w("URL_SERVICE", "Error getting chatbot URL: ", exception)
            }
    }
}