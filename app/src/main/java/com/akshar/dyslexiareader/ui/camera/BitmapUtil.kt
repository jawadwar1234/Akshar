package com.akshar.dyslexiareader.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object BitmapUtil {

    /**
     * Loads and downsamples a bitmap from Uri to prevent OutOfMemory errors on 2-3GB RAM phones.
     */
    fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int = 1600, reqHeight: Int = 2000): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // Low memory footprint for 2-3GB RAM

            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Rotates bitmap by given degrees.
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Crops bitmap according to normalized RectF (0.0 to 1.0).
     */
    fun cropBitmap(bitmap: Bitmap, normalizedCropRect: RectF): Bitmap {
        val left = (normalizedCropRect.left.coerceIn(0f, 1f) * bitmap.width).toInt()
        val top = (normalizedCropRect.top.coerceIn(0f, 1f) * bitmap.height).toInt()
        val width = ((normalizedCropRect.width().coerceIn(0.1f, 1f)) * bitmap.width).toInt().coerceAtMost(bitmap.width - left)
        val height = ((normalizedCropRect.height().coerceIn(0.1f, 1f)) * bitmap.height).toInt().coerceAtMost(bitmap.height - top)

        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    /**
     * Enhances contrast and converts to grayscale for clearer OCR on budget cameras.
     */
    fun enhanceTextContrast(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()

        // Grayscale + Contrast stretch
        val cm = ColorMatrix().apply {
            setSaturation(0f)
            val scale = 1.35f
            val translate = (-0.35f * 128f)
            postConcat(
                ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return output
    }

    /**
     * Saves processed bitmap to app's local private internal storage directory.
     * Keeps images safe and persistent in app-private storage across app restarts.
     */
    fun saveBitmapToAppPrivateStorage(context: Context, bitmap: Bitmap, fileName: String): File {
        val scansDir = File(context.filesDir, "scanned_documents").apply {
            if (!exists()) mkdirs()
        }
        val file = File(scansDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    }

    /**
     * Saves processed bitmap to app's local private cache directory.
     */
    fun saveBitmapToLocalCache(context: Context, bitmap: Bitmap, fileName: String): File {
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    }
}
