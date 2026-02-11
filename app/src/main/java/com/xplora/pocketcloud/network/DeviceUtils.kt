package com.xplora.pocketcloud.network

import android.os.Build

object DeviceUtils {

    fun isEmulator(): Boolean {
        return (
                Build.FINGERPRINT.startsWith("generic")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for")
                        || Build.MANUFACTURER.contains("Genymotion")
                )
    }
}