package com.xplora.pocketcloud.consumer

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xplora.pocketcloud.R
import com.xplora.pocketcloud.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.animation.ObjectAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
class ConsumerImageActivity : AppCompatActivity() {


    lateinit var card: View
    lateinit var imgFront: ImageView
    lateinit var imgBack: ImageView


    private val PICK_IMAGE = 101


    private lateinit var spinnerTask: Spinner
    private lateinit var btnPick: Button
    private lateinit var btnSubmit: Button
    private lateinit var progress: ProgressBar

    private var selectedImage: Uri? = null
    private var jobId: String? = null

    private val consumerId = "consumer-1" // static for now

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consumer_image)


        spinnerTask = findViewById(R.id.spinnerTask)
        btnPick = findViewById(R.id.btnPick)
        btnSubmit = findViewById(R.id.btnSubmit)
        progress = findViewById(R.id.progressBar)
        card = findViewById(R.id.cardContainer)
        imgFront = findViewById(R.id.imgFront)
        imgBack = findViewById(R.id.imgBack)
        card.cameraDistance = 8000 * resources.displayMetrics.density

// Better 3D depth
        card.cameraDistance = 8000 * resources.displayMetrics.density

        spinnerTask.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("grayscale", "edge")
        )

        btnPick.setOnClickListener {
            startActivityForResult(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ),
                PICK_IMAGE
            )
        }

        btnSubmit.setOnClickListener {
            if (selectedImage == null) {
                toast("Pick an image first")
            } else {
                submitImage()
            }
        }
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == PICK_IMAGE && res == Activity.RESULT_OK) {
            selectedImage = data?.data
            imgFront.setImageURI(selectedImage)

// reset card state
            imgFront.visibility = View.VISIBLE
            imgBack.visibility = View.GONE
            card.rotationY = 0f
        }
    }


    private fun flipCard() {
        val flipOut = ObjectAnimator.ofFloat(card, "rotationY", 0f, 90f)
        flipOut.duration = 250

        val flipIn = ObjectAnimator.ofFloat(card, "rotationY", -90f, 0f)
        flipIn.duration = 250

        flipOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                imgFront.visibility = View.GONE
                imgBack.visibility = View.VISIBLE
                flipIn.start()
            }
        })

        flipOut.start()
    }
    private fun submitImage() {
        val uri = selectedImage ?: return
        val task = spinnerTask.selectedItem.toString()

        progress.visibility = ProgressBar.VISIBLE
        btnSubmit.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {

            val file = File(cacheDir, "upload.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val imagePart = MultipartBody.Part.createFormData(
                "image",
                file.name,
                file.asRequestBody("image/*".toMediaType())
            )

            val taskBody = task.toRequestBody("text/plain".toMediaType())
            val consumerIdBody =
                consumerId.toRequestBody("text/plain".toMediaType())

            val resp = RetrofitClient.rawApi
                .submitJob(imagePart, consumerIdBody, taskBody)
                .execute()

            if (!resp.isSuccessful) {
                runOnUiThread {
                    progress.visibility = ProgressBar.GONE
                    btnSubmit.isEnabled = true
                    toast("Upload failed")
                }
                return@launch
            }

            jobId = resp.body()?.string()?.trim()
            pollResult()
        }
    }

    private fun pollResult() {
        val id = jobId ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                val resp = RetrofitClient.rawApi.getResult(id).execute()

                if (resp.code() == 202) {
                    Thread.sleep(2000)
                    continue
                }

                val bytes = resp.body()?.bytes()
                if (bytes != null) {
                    val bmp =
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    runOnUiThread {
                        runOnUiThread {
                            imgBack.setImageBitmap(bmp)
                            flipCard()
                            progress.visibility = View.GONE
                            btnSubmit.isEnabled = true
                        }
                        progress.visibility = ProgressBar.GONE
                        btnSubmit.isEnabled = true
                        toast("Processing complete")
                    }
                }
                break
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}