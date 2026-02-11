package com.xplora.pocketcloud.service
import com.xplora.pocketcloud.image.ImageProcessor

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.xplora.pocketcloud.R
import com.xplora.pocketcloud.network.ApiService
import com.xplora.pocketcloud.network.HttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.UUID
import android.util.Log
import org.json.JSONObject
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.ByteArrayOutputStream
import android.util.Base64


class ProviderService : Service() {

    private val channelId = "pocketcloud_provider"
    private lateinit var deviceId: String

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, buildNotification())

        deviceId = UUID.randomUUID().toString()
        register()
        startPolling()
    }

    private fun startPolling() {
        Thread {
            while (true) {
                try {
                    val req = ApiService.nextChunk(deviceId)
                    val resp = HttpClient.client.newCall(req).execute()
                    val body = resp.body?.string() ?: ""

                    if (!body.trim().startsWith("{")) {
                        Log.e("PocketCloud", "Server error: $body")
                        Thread.sleep(3000)
                        continue
                    }

                    if (body.contains("idle")) {
                        Log.d("PocketCloud", "No work. Sleeping...")
                    } else {
                        Log.d("PocketCloud", "Chunk received: $body")
                        processChunk(body)
                    }

                    Thread.sleep(3000)

                } catch (e: Exception) {
                    Log.e("PocketCloud", "Polling error", e)
                    Thread.sleep(3000)
                }
            }
        }.start()
    }


    private fun processChunk(chunkJson: String) {
        val chunkId = chunkJson.substringAfter("\"id\":\"").substringBefore("\"")
        val jobId = chunkJson.substringAfter("\"job_id\":\"").substringBefore("\"")
        val data = chunkJson.substringAfter("\"data\":\"").substringBefore("\"")

        Log.d("PocketCloud", "Processing chunk: $chunkId")

        val bytes = Base64.decode(data, Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val gray = toGrayScale(bmp)

        val out = ByteArrayOutputStream()
        gray.compress(Bitmap.CompressFormat.PNG, 100, out)
        val newBase64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)

        val req = ApiService.submitResult(jobId, chunkId, newBase64)
        HttpClient.client.newCall(req).execute()

        Log.d("PocketCloud", "Result submitted for $chunkId")
    }

    private fun toGrayScale(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val p = src.getPixel(x, y)

                val r = Color.red(p)
                val g = Color.green(p)
                val b = Color.blue(p)

                val gray = (0.3 * r + 0.59 * g + 0.11 * b).toInt()
                bmpGray.setPixel(x, y, Color.rgb(gray, gray, gray))
            }
        }
        return bmpGray
    }





    private fun register() {
        val id = UUID.randomUUID().toString()
        Log.d("PocketCloud", "Registering with id: $id")

        val req = ApiService.register(id, "0.0.0.0")

        HttpClient.client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PocketCloud", "Register failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("PocketCloud", "Register success: ${response.code}")
            }
        })
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "PocketCloud Provider",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("PocketCloud")
            .setContentText("Running as Provider")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
