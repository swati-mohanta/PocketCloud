package com.xplora.pocketcloud.image

import android.graphics.*
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageProcessor {

    fun process(base64: String, task: String): String {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        val out = when (task) {
            "grayscale" -> gray(bmp)
            "edge" -> edge(bmp)
            else -> bmp
        }

        val stream = ByteArrayOutputStream()
        out.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun gray(src: Bitmap): Bitmap {
        val bmp = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val paint = Paint()
        val m = ColorMatrix()
        m.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(m)
        c.drawBitmap(src, 0f, 0f, paint)
        return bmp
    }

    private fun edge(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        for (x in 1 until w-1) {
            for (y in 1 until h-1) {
                val c = Color.red(src.getPixel(x,y))
                val cx = Color.red(src.getPixel(x+1,y))
                val cy = Color.red(src.getPixel(x,y+1))
                val g = kotlin.math.abs(c - cx) + kotlin.math.abs(c - cy)
                out.setPixel(x,y, Color.rgb(g,g,g))
            }
        }
        return out
    }
}
