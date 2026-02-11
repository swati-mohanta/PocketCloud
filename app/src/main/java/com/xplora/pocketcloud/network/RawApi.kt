package com.xplora.pocketcloud.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface RawApi {

    // -------- CONSUMER --------

    @Multipart
    @POST("/consumer/submit")
    fun submitJob(
        @Part image: MultipartBody.Part,
        @Part("consumerId") consumerId: RequestBody,
        @Part("task") task: RequestBody
    ): Call<ResponseBody>

    @GET("/consumer/result/{jobId}")
    fun getResult(
        @Path("jobId") jobId: String
    ): Call<ResponseBody>

    // ✅ ADD THIS (FIX)
    @GET("/prime/result/{jobId}")
    fun getPrimeResult(
        @Path("jobId") jobId: String
    ): Call<ResponseBody>

    @GET("/consumer/tokens/{consumerId}")
    fun getConsumerTokens(
        @Path("consumerId") consumerId: String
    ): Call<ResponseBody>

    // -------- PROVIDER --------

    @GET("/provider/next/{providerId}")
    fun getNextChunk(
        @Path("providerId") providerId: String
    ): Call<ResponseBody>

    @POST("/provider/submit")
    fun submitChunk(
        @Header("Provider-Id") providerId: String,
        @Header("Chunk-Id") chunkId: String,
        @Header("Job-Id") jobId: String,
        @Body data: RequestBody
    ): Call<ResponseBody>

    @GET("/provider/{providerId}/tokens")
    fun getProviderTokens(
        @Path("providerId") providerId: String
    ): Call<ResponseBody>
}