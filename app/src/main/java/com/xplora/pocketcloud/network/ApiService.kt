package com.xplora.pocketcloud.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("consumer/submit")
    fun submitJob(
        @Part image: MultipartBody.Part,
        @Part("consumerId") consumerId: RequestBody,
        @Part("task") task: RequestBody
    ): Call<ResponseBody>

    @GET("consumer/result/{jobId}")
    fun getResult(
        @Path("jobId") jobId: String
    ): Call<ResponseBody>



    @GET("consumer/{consumerId}/tokens")
    fun getConsumerTokens(
        @Path("consumerId") consumerId: String
    ): Call<ResponseBody>

    @GET("provider/{providerId}/tokens")
    fun getProviderTokens(
        @Path("providerId") providerId: String
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("prime/submit")
    fun submitPrimeJob(
        @Field("consumerId") consumerId: String,
        @Field("max") max: Int
    ): Call<ResponseBody>

    @GET("prime/result/{jobId}")
    fun getPrimeResult(
        @Path("jobId") jobId: String
    ): Call<ResponseBody>
}