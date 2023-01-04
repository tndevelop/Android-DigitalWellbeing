package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.example.myapplication.R

class PopUpWindow : AppCompatActivity() {

    private var popupTitle = ""
    private var popupText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.popup_window)

        val bundle = intent.extras
        popupTitle = bundle?.getString("popupTitle", "Title") ?: ""
        popupText = bundle?.getString("popupText", "Text") ?: ""

        val title = findViewById<AppCompatTextView>(R.id.popup_window_title)
        val text = findViewById<AppCompatTextView>(R.id.popup_window_text)
        val button = findViewById<Button>(R.id.popup_window_button)
        title.text = popupTitle
        text.text = popupText
        button.setOnClickListener {
            homePage()
        }
    }

    private fun closeApp() {
        moveTaskToBack(true)
        finish()
    }

    private fun homePage(){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed(){
        homePage()
    }


}