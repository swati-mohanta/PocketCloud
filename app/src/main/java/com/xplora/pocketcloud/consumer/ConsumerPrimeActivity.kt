package com.xplora.pocketcloud.consumer

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xplora.pocketcloud.R
import com.xplora.pocketcloud.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConsumerPrimeActivity : AppCompatActivity() {

    private lateinit var etMax: EditText
    private lateinit var btnStart: Button
    private lateinit var tvResult: TextView
    private lateinit var progress: ProgressBar

    private var jobId: String? = null
    private val consumerId = "consumer-1" // static for now

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consumer_prime)

        etMax = findViewById(R.id.etMax)
        btnStart = findViewById(R.id.btnStart)
        tvResult = findViewById(R.id.tvResult)
        progress = findViewById(R.id.progressBar)

        btnStart.setOnClickListener {
            val max = etMax.text.toString().toIntOrNull()
            if (max == null || max < 10_000) {
                toast("Enter number ≥ 10000")
                return@setOnClickListener
            }
            submitPrimeJob(max)
        }
    }

    // --------------------------------------------------
    // SUBMIT PRIME JOB
    // --------------------------------------------------
    private fun submitPrimeJob(max: Int) {
        progress.visibility = ProgressBar.VISIBLE
        tvResult.text = ""
        btnStart.isEnabled = false

        RetrofitClient.api
            .submitPrimeJob(consumerId, max)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (!response.isSuccessful) {
                        uiFail("Submit failed")
                        return
                    }

                    jobId = response.body()?.string()?.trim()
                    pollPrimeResult()
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                    uiFail("Network error")
                }
            })
    }

    // --------------------------------------------------
    // POLL RESULT
    // --------------------------------------------------
    private fun pollPrimeResult() {
        val id = jobId ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                val resp =
                    RetrofitClient.api.getPrimeResult(id).execute()

                if (resp.code() == 202) {
                    Thread.sleep(2000)
                    continue
                }

                val body = resp.body()?.string()

                withContext(Dispatchers.Main) {
                    progress.visibility = ProgressBar.GONE
                    btnStart.isEnabled = true
                    tvResult.text =
                        body?.let { "Total primes: $it" } ?: "No result"
                }
                break
            }
        }
    }

    // --------------------------------------------------
    // UI HELPERS
    // --------------------------------------------------
    private fun uiFail(msg: String) {
        runOnUiThread {
            progress.visibility = ProgressBar.GONE
            btnStart.isEnabled = true
            toast(msg)
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}