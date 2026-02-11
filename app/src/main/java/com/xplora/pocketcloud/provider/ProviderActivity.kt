package com.xplora.pocketcloud.provider

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xplora.pocketcloud.R
import android.os.Build
import androidx.activity.OnBackPressedCallback

class ProviderActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvTokens: TextView
    private lateinit var btnStop: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)   // ✅ MUST BE FIRST
        setContentView(R.layout.activity_provider)

        // ---- Handle back button safely (modern way) ----
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    moveTaskToBack(true)
                }
            }
        )

        // ---- Start ProviderService HERE ----
        val intent = Intent(this, ProviderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        // ---- UI setup ----
        tvStatus = findViewById(R.id.tvStatus)
        tvTokens = findViewById(R.id.tvTokens)
        btnStop = findViewById(R.id.btnStop)

        tvStatus.text = "Providing compute power…"
        tvTokens.text = "Tokens: 0"

        btnStop.setOnClickListener {
            stopService(Intent(this, ProviderService::class.java))
            finish()
        }
    }
    override fun onResume() {
        super.onResume()
        startTokenPolling()
    }



    private fun startTokenPolling() {
        tvTokens.postDelayed(object : Runnable {
            override fun run() {
                // TEMP: fake update (we’ll connect backend next)
                val current = tvTokens.text.toString()
                    .substringAfter(": ")
                    .toIntOrNull() ?: 0
                tvTokens.text = "Tokens: ${current + 1}"

                tvTokens.postDelayed(this, 3000)
            }
        }, 3000)
    }



}


