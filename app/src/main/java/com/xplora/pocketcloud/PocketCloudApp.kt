package com.xplora.pocketcloud

import android.app.Application
import okhttp3.OkHttpClient
import java.util.logging.Level
import java.util.logging.Logger

class PocketCloudApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Logger.getLogger(OkHttpClient::class.java.name).level = Level.WARNING
        Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
    }
}