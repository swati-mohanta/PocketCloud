package com.xplora.pocketcloud.provider

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import com.xplora.pocketcloud.network.DeviceUtils
import com.xplora.pocketcloud.network.ServerConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.concurrent.thread

class ProviderService : Service() {

    companion object {
        private const val LIFE = "PROVIDER_LIFECYCLE"
        private const val TAG = "PocketCloud-Provider"
    }

    init {
        Log.e(LIFE, "ProviderService INSTANCE CREATED")
    }

    private val BASE_URL =
        ServerConfig.baseUrl(DeviceUtils.isEmulator())

    private val client = OkHttpClient()
    private val providerId: String by lazy {
        val prefs = getSharedPreferences("provider_prefs", MODE_PRIVATE)
        prefs.getString("provider_id", null)
            ?: UUID.randomUUID().toString().also {
                prefs.edit().putString("provider_id", it).apply()
            }
    }

    @Volatile
    private var running = false

    // ----------------------------------------------------
    // LIFECYCLE
    // ----------------------------------------------------
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        Log.e(LIFE, "onStartCommand CALLED")

        startForeground(1, buildNotification())

        if (!running) {
            running = true
            Log.e(LIFE, "Starting polling loop")
            startPolling()
        }

        return START_STICKY
    }

    // ----------------------------------------------------
    // POLLING LOOP
    // ----------------------------------------------------
    private fun startPolling() {
        thread {
            Log.e(LIFE, "Polling THREAD STARTED | id=$providerId")

            while (running) {
                try {
                    val pollReq = Request.Builder()
                        .url("$BASE_URL/provider/next/$providerId")
                        .get()
                        .build()

                    client.newCall(pollReq).execute().use { resp ->

                        // ---- NO WORK ----
                        if (resp.code == 204) {
                            Log.d(TAG, "No chunk available")
                            Thread.sleep(4000)
                            continue
                        }

                        // ---- BAD RESPONSE ----
                        if (!resp.isSuccessful || resp.body == null) {
                            Log.w(TAG, "Bad response ${resp.code}")
                            Thread.sleep(4000)
                            return@use
                        }

                        val chunkId = resp.header("Chunk-Id")
                        val jobId = resp.header("Job-Id")
                        val task = resp.header("Task") ?: "grayscale"

                        if (chunkId == null || jobId == null) {
                            Log.e(TAG, "Missing headers")
                            return@use
                        }

                        val bytes = resp.body!!.bytes()
                        Log.d(TAG, "Chunk received | task=$task | bytes=${bytes.size}")

                        // ==================================================
                        // PRIME TASK
                        // ==================================================
                        if (task == "prime") {
                            val text = String(bytes)
                            val (start, end) = text.split(",").map { it.toInt() }

                            Log.d(TAG, "Prime task: $start → $end")

                            val count =
                                PrimeWorker.countPrimes(start, end)

                            submitResult(
                                chunkId,
                                jobId,
                                count.toString().toByteArray()
                            )

                            fetchProviderTokens()
                            return@use
                        }

                        // ==================================================
                        // IMAGE TASK
                        // ==================================================
                        val bmp =
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                ?: run {
                                    Log.e(TAG, "Bitmap decode failed")
                                    return@use
                                }

                        val processed =
                            ImageProcessor.process(bmp, task, providerId)

                        bmp.recycle()

                        val out = ByteArrayOutputStream()
                        processed.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        processed.recycle()

                        submitResult(chunkId, jobId, out.toByteArray())
                        fetchProviderTokens()
                    }

                    Thread.sleep(3000)

                } catch (e: Exception) {
                    Log.e(TAG, "Provider error", e)
                    Thread.sleep(5000)
                }
            }
        }
    }

    // ----------------------------------------------------
    // SUBMIT RESULT
    // ----------------------------------------------------
    private fun submitResult(
        chunkId: String,
        jobId: String,
        data: ByteArray
    ) {
        val submitReq = Request.Builder()
            .url("$BASE_URL/provider/submit")
            .post(
                RequestBody.create(
                    "application/octet-stream".toMediaType(),
                    data
                )
            )
            .addHeader("Provider-Id", providerId)
            .addHeader("Chunk-Id", chunkId)
            .addHeader("Job-Id", jobId)
            .build()

        client.newCall(submitReq).execute().use {
            Log.d(TAG, "Chunk submitted")
        }
    }

    // ----------------------------------------------------
    // TOKENS
    // ----------------------------------------------------
    private fun fetchProviderTokens() {
        val req = Request.Builder()
            .url("$BASE_URL/provider/$providerId/tokens")
            .build()

        client.newCall(req).execute().use { resp ->
            Log.d(TAG, "Tokens = ${resp.body?.string()}")
        }
    }

    // ----------------------------------------------------
    // NOTIFICATION
    // ----------------------------------------------------
    private fun buildNotification(): Notification {
        val channelId = "provider_channel"
        val manager = getSystemService(NotificationManager::class.java)

        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "PocketCloud Provider",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        return Notification.Builder(this, channelId)
            .setContentTitle("PocketCloud Provider")
            .setContentText("Contributing compute")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
    }

    override fun onDestroy() {
        Log.e(LIFE, "ProviderService DESTROYED")
        running = false
        super.onDestroy()
    }



    override fun onBind(intent: Intent?): IBinder? = null
}