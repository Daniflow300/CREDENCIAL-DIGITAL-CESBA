package com.cesba.credencial

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import java.io.File

object PhotoUtils {
    /** Carga la foto respetando la rotación y reducida de tamaño. */
    fun load(file: File, maxSize: Int): Bitmap {
        val source = ImageDecoder.createSource(file)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val biggest = maxOf(info.size.width, info.size.height)
            val sample = (biggest / maxSize).coerceAtLeast(1)
            decoder.setTargetSampleSize(sample)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }
}