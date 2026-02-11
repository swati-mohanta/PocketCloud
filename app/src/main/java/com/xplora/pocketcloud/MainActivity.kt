package com.xplora.pocketcloud

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.xplora.pocketcloud.consumer.ConsumerTaskActivity
import com.xplora.pocketcloud.provider.ProviderActivity
import com.xplora.pocketcloud.provider.ProviderService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Consumer mode
        findViewById<Button>(R.id.btnConsumer).setOnClickListener {
            startActivity(
                Intent(this, ConsumerTaskActivity::class.java)
            )
        }

        // Provider mode (foreground service)
        findViewById<Button>(R.id.btnProvider).setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        101
                    )
                    return@setOnClickListener
                }
            }

            findViewById<Button>(R.id.btnProvider).setOnClickListener {
                startActivity(
                    Intent(this, ProviderActivity::class.java)
                )
            }
        }
    }
}