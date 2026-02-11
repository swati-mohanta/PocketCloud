package com.xplora.pocketcloud.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {

    private val client = OkHttpClient.Builder().build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ServerConfig.baseUrl(DeviceUtils.isEmulator()))
        .addConverterFactory(ScalarsConverterFactory.create())
        .client(client)
        .build()

    // ✅ Consumer API (JSON / form / result)
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // ✅ Provider / raw binary API
    val rawApi: RawApi by lazy {
        retrofit.create(RawApi::class.java)
    }
}