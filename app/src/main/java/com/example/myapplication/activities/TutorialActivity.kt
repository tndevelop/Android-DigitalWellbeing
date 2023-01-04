package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroCustomLayoutFragment

class TutorialActivity: AppIntro2() {
    private var firstOpening: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firstOpening = intent.getBooleanExtra("FIRST_OPENING", true)

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_tutorial_0))

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_tutorial_1))

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_tutorial_2))

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_tutorial_3))

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_tutorial_4))

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_tutorial_5))

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.fragment_tutorial_6))

        setIndicatorColor(
            selectedIndicatorColor = getColor(R.color.primary),
            unselectedIndicatorColor = getColor(R.color.black)
        )
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)

        if(firstOpening!= null && firstOpening == true) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        if(firstOpening!= null && firstOpening == true) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }


}