package com.xplora.pocketcloud.consumer

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.xplora.pocketcloud.R

class ConsumerTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consumer_task)

        val imageTask = findViewById<LinearLayout>(R.id.cardImageTask)
        val primeTask = findViewById<LinearLayout>(R.id.cardPrimeTask)

        imageTask.setOnClickListener {
            startActivity(
                Intent(this, ConsumerImageActivity::class.java)
            )
        }

        primeTask.setOnClickListener {
            startActivity(
                Intent(this, ConsumerPrimeActivity::class.java)
            )
        }
    }
}