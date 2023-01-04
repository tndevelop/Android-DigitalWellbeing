package com.example.myapplication.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.example.myapplication.R
import com.example.myapplication.utils.Constants


class DummyBrightnessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val brightnessIntent = this.intent
        val brightness = brightnessIntent.getFloatExtra("brightness value", 0f)
        val lp = window.attributes
        lp.screenBrightness = brightness
        window.attributes = lp

        //this next line is very important, you need to finish your activity with slight delay
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, Constants.FORWARDING_DELAY)

    }
}