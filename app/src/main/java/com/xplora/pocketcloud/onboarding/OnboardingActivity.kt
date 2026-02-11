package com.xplora.pocketcloud.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.xplora.pocketcloud.R

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val btnNext = findViewById<Button>(R.id.btnNext)

        val pages = listOf(
            OnboardingItem(
                "Welcome to PocketCloud",
                "Turn idle devices into distributed compute power.",
                R.drawable.onboard_1
            ),
            OnboardingItem(
                "How it Works",
                "Tasks are split, processed by providers, and merged securely.",
                R.drawable.onboard_2
            ),
            OnboardingItem(
                "Earn & Spend Tokens",
                "Providers earn tokens. Consumers spend tokens fairly.",
                R.drawable.onboard_3
            )
        )

        viewPager.adapter = OnboardingAdapter(pages)

        btnNext.setOnClickListener {
            if (viewPager.currentItem < pages.size - 1) {
                viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, RoleSelectionActivity::class.java))
                finish()
            }
        }
    }
}