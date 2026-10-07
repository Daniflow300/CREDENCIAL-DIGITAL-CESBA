package com.cesba.credencial

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrStyler {

    fun create(content: String, sizePx: Int, blue: Int, blueDark: Int, orange: Int): Bitmap {
        val hints = mapOf<EncodeHintType, Any>(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 0,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        val n = matrix.width

        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        val m = sizePx.toFloat() / n

        // Zona central reservada para el escudo
        var logoModules = (n * 0.22f).toInt()
        if ((n - logoModules) % 2 != 0) logoModules++
        val start = (n - logoModules) / 2
        val end = start + logoModules

        // Puntos redondos con degradado
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, sizePx.toFloat(), sizePx.toFloat(),
                blue, blueDark, Shader.TileMode.CLAMP
            )
        }
        for (y in 0 until n) {
            for (x in 0 until n) {
                if (!matrix.get(x, y)) continue
                if (isFinder(x, y, n)) continue
                if (x in start until end && y in start until end) continue
                canvas.drawCircle((x + 0.5f) * m, (y + 0.5f) * m, m * 0.46f, dotPaint)
            }
        }

        // Ojos de posición: marco azul, centro naranja
        val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = blueDark }
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val orangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = orange }
        for ((ox, oy) in listOf(0 to 0, n - 7 to 0, 0 to n - 7)) {
            val l = ox * m
            val t = oy * m
            canvas.drawRoundRect(RectF(l, t, l + 7 * m, t + 7 * m), 2 * m, 2 * m, bluePaint)
            canvas.drawRoundRect(RectF(l + m, t + m, l + 6 * m, t + 6 * m), 1.3f * m, 1.3f * m, whitePaint)
            canvas.drawRoundRect(RectF(l + 2 * m, t + 2 * m, l + 5 * m, t + 5 * m), m, m, orangePaint)
        }

        // Escudo central con la "C" de CESBA
        val cx = sizePx / 2f
        val r = logoModules * m / 2f
        canvas.drawCircle(cx, cx, r, whitePaint)
        canvas.drawCircle(cx, cx, r * 0.9f, orangePaint)
        canvas.drawCircle(cx, cx, r * 0.76f, bluePaint)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = r * 1.15f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val fm = textPaint.fontMetrics
        canvas.drawText("C", cx, cx - (fm.ascent + fm.descent) / 2f, textPaint)

        return bmp
    }

    private fun isFinder(x: Int, y: Int, n: Int) =
        (x < 7 && y < 7) || (x >= n - 7 && y < 7) || (x < 7 && y >= n - 7)
}