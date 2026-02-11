package com.xplora.pocketcloud.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClient {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}
