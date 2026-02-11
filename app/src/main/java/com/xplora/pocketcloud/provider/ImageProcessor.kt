package com.xplora.pocketcloud.provider

import android.graphics.*
import kotlin.math.abs

object ImageProcessor {

    fun process(
        src: Bitmap,
        task: String,
        providerId: String
    ): Bitmap {
        val base = when (task) {
            "edge" -> edge(src)
            else -> gray(src)
        }
        val watermarked = watermark(base, providerId)
        base.recycle()
        return watermarked
    }

    private fun gray(src: Bitmap): Bitmap {
        val out =
            Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        for (y in 0 until src.height)
            for (x in 0 until src.width) {
                val p = src.getPixel(x, y)
                val g =
                    (0.3 * Color.red(p) +
                            0.59 * Color.green(p) +
                            0.11 * Color.blue(p)).toInt()
                out.setPixel(x, y, Color.rgb(g, g, g))
            }
        return out
    }

    private fun edge(src: Bitmap): Bitmap {
        val out =
            Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        for (y in 1 until src.height - 1)
            for (x in 1 until src.width - 1) {
                val c = Color.red(src.getPixel(x, y))
                val cx = Color.red(src.getPixel(x + 1, y))
                val cy = Color.red(src.getPixel(x, y + 1))
                val g = abs(c - cx) + abs(c - cy)
                out.setPixel(x, y, Color.rgb(g, g, g))
            }
        return out
    }

    private fun watermark(
        src: Bitmap,
        providerId: String
    ): Bitmap {
        val mutable =
            src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val bgPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0)
        }

        val textPaint = Paint().apply {
            color = Color.RED
            textSize = 40f
            isAntiAlias = true
            isFakeBoldText = true
        }

        val label = "Provider: ${providerId.take(6)}"
        val textWidth = textPaint.measureText(label)
        val textHeight = textPaint.textSize

        val x = 12f
        val y = mutable.height - 12f

        canvas.drawRect(
            x - 8,
            y - textHeight - 8,
            x + textWidth + 8,
            y + 8,
            bgPaint
        )
        canvas.drawText(label, x, y, textPaint)

        return mutable
    }
}