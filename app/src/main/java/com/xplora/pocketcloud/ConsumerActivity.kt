package com.xplora.pocketcloud

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ConsumerActivity : AppCompatActivity() {

    private val PICK_IMAGE = 1001
    private var selectedImage: Uri? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consumer)

        val btnPick = findViewById<Button>(R.id.btnPick)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val spinner = findViewById<Spinner>(R.id.spinnerTask)
        val imageView = findViewById<ImageView>(R.id.imageView)

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("edge", "grayscale")
        )

        btnPick.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, PICK_IMAGE)
        }

        btnSubmit.setOnClickListener {
            if (selectedImage == null) {
                Toast.makeText(this, "Pick an image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            submitJob(spinner.selectedItem.toString(), imageView)
        }
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == PICK_IMAGE && res == Activity.RESULT_OK) {
            selectedImage = data?.data
            findViewById<ImageView>(R.id.imageView).setImageURI(selectedImage)
        }
    }

    private fun submitJob(task: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            val stream = contentResolver.openInputStream(selectedImage!!)!!
            val bytes = stream.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            val json = JSONObject()
            json.put("image_base64", base64)
            json.put("task", task)

            val body = json.toString()
                .toRequestBody("application/json".toMediaType())

            val req = Request.Builder()
                .url("http://10.0.2.2:8000/submit-image-job")
                .post(body)
                .build()

            val resp = client.newCall(req).execute()
            val jobId = JSONObject(resp.body!!.string()).getString("job_id")

            pollResult(jobId, imageView)
        }
    }

    private suspend fun pollResult(jobId: String, imageView: ImageView) {
        while (true) {
            delay(2000)

            val req = Request.Builder()
                .url("http://172.20.10.10:8000/job-result/$jobId")
                .build()

            val resp = client.newCall(req).execute()
            val body = resp.body?.string() ?: continue

            val json = JSONObject(body)
            val status = json.getString("status")

            if (status == "done") {
                val base64 = json.getString("image_base64")
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bmp)
                    Toast.makeText(this@ConsumerActivity, "Task completed", Toast.LENGTH_SHORT).show()
                }
                break
            } else {
                Log.d("PocketCloud", "Job $jobId status: $status")
            }
        }
    }

}
