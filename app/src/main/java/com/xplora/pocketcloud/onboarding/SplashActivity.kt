package com.xplora.pocketcloud.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.xplora.pocketcloud.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                Intent(this, OnboardingActivity::class.java)
            )
            finish()
        }, 2800) // 1.8 seconds
    }
}