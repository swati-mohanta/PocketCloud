package com.xplora.pocketcloud.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.xplora.pocketcloud.R
import com.xplora.pocketcloud.consumer.ConsumerTaskActivity
import com.xplora.pocketcloud.provider.ProviderActivity

class RoleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔴 THIS MUST MATCH THE LAYOUT FILE
        setContentView(R.layout.activity_role_selection)

        findViewById<Button>(R.id.btnConsumer).setOnClickListener {
            startActivity(Intent(this, ConsumerTaskActivity::class.java))
        }

        findViewById<Button>(R.id.btnProvider).setOnClickListener {
            startActivity(Intent(this, ProviderActivity::class.java))
        }
    }
}