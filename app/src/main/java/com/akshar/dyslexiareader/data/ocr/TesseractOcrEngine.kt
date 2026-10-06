package com.akshar.dyslexiareader.data.ocr

import android.content.Context
import android.graphics.Bitmap
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.ocr.OcrEngine
import com.akshar.dyslexiareader.domain.ocr.OcrResult
import com.akshar.dyslexiareader.domain.util.ScriptDetector
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TesseractOcrEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : OcrEngine {

    private val tessDataDir: File by lazy {
        File(context.filesDir, "tessdata").apply { mkdirs() }
    }

    override suspend fun recognizeText(bitmap: Bitmap): OcrResult = withContext(Dispatchers.Default) {
        var tessApi: TessBaseAPI? = null
        try {
            tessApi = TessBaseAPI()
            val dataPath = context.filesDir.absolutePath

            // Check if Gujarati or Bengali traineddata exists, or initialize multi-language
            val initSuccess = try {
                tessApi.init(dataPath, "guj+ben+hin+eng")
            } catch (e: Exception) {
                false
            }

            if (!initSuccess) {
                return@withContext OcrResult(
                    text = "",
                    isTesseractBetaUsed = true
                )
            }

            tessApi.setImage(bitmap)
            val extracted = tessApi.utF8Text ?: ""
            val script = ScriptDetector.detectScript(extracted)

            OcrResult(
                text = extracted,
                detectedScript = script,
                isTesseractBetaUsed = true
            )
        } catch (e: Exception) {
            OcrResult(text = "", isTesseractBetaUsed = true)
        } finally {
            try {
                tessApi?.recycle()
            } catch (ignored: Exception) {}
        }
    }

    /**
     * Helper to copy traineddata models from assets to internal storage if bundled.
     */
    suspend fun copyAssetDataIfNeeded(languageCode: String) = withContext(Dispatchers.IO) {
        val destFile = File(tessDataDir, "$languageCode.traineddata")
        if (!destFile.exists()) {
            try {
                context.assets.open("tessdata/$languageCode.traineddata").use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                // If not in assets, it will degrade gracefully
            }
        }
    }
}
