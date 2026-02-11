package com.xplora.pocketcloud.network

object ServerConfig {

    // CHANGE THIS WHEN USING REAL PHONE
    private const val REAL_DEVICE_IP = "10.15.7.20"

    private const val PORT = "8080"

    fun baseUrl(isEmulator: Boolean): String {
        return if (isEmulator) {
            "http://10.0.2.2:$PORT"
        } else {
            "http://$REAL_DEVICE_IP:$PORT"
        }
    }
}