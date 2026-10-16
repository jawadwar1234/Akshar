package com.akshar.dyslexiareader.data.ocr

import android.content.Context
import android.graphics.Bitmap
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.ocr.OcrEngine
import com.akshar.dyslexiareader.domain.ocr.OcrResult
import com.akshar.dyslexiareader.domain.util.ScriptDetector
import com.akshar.dyslexiareader.domain.util.TextCleanupUtil
import com.akshar.dyslexiareader.ui.camera.BitmapUtil
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TesseractOcrEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val languageManager: TesseractLanguageManager
) : OcrEngine {

    override suspend fun recognizeText(bitmap: Bitmap): OcrResult = recognizeText(bitmap, null)

    /**
     * Performs Tesseract OCR using specified language or available installed models.
     * Reuses contrast preprocessing and cleans output with TextCleanupUtil.
     */
    suspend fun recognizeText(bitmap: Bitmap, targetLanguage: String? = null): OcrResult = withContext(Dispatchers.Default) {
        var tessApi: TessBaseAPI? = null
        try {
            val dataPath = context.filesDir.absolutePath

            // Determine which language models are available
            val activeLang = if (!targetLanguage.isNullOrBlank() && languageManager.isLanguageDownloaded(targetLanguage)) {
                targetLanguage
            } else {
                // Find all installed languages, prioritizing Gujarati and Bengali if present
                val installed = listOf("guj", "ben", "hin", "eng").filter { languageManager.isLanguageDownloaded(it) }
                if (installed.isNotEmpty()) {
                    installed.joinToString("+")
                } else {
                    "eng"
                }
            }

            // Ensure destination tessdata directory exists
            languageManager.tessDataDir

            tessApi = TessBaseAPI()
            val initSuccess = try {
                // Initialize with LSTM engine mode (OEM_LSTM_ONLY = 1)
                tessApi.init(dataPath, activeLang, TessBaseAPI.OEM_LSTM_ONLY)
            } catch (e: Exception) {
                // Fallback to default init
                try {
                    tessApi.init(dataPath, activeLang)
                } catch (_: Exception) {
                    false
                }
            }

            if (!initSuccess) {
                return@withContext OcrResult(
                    text = "",
                    isTesseractBetaUsed = true
                )
            }

            // Set Page Segmentation Mode to Auto (PSM_AUTO = 3)
            try {
                tessApi.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
            } catch (_: Exception) {}

            // 1. Preprocess: enhance contrast and optimize for OCR
            val preprocessedBitmap = try {
                BitmapUtil.enhanceTextContrast(bitmap)
            } catch (_: Exception) {
                bitmap
            }

            // 2. Perform OCR
            tessApi.setImage(preprocessedBitmap)
            val rawExtracted = tessApi.utF8Text ?: ""

            // 3. Post-process: clean hyphens, normalize line breaks and Indic punctuation
            val cleanedText = TextCleanupUtil.cleanupOcrText(rawExtracted)
            val script = ScriptDetector.detectScript(cleanedText)

            val isBeta = script == SupportedScript.GUJARATI || script == SupportedScript.BENGALI ||
                activeLang.contains("guj") || activeLang.contains("ben")

            OcrResult(
                text = cleanedText,
                detectedScript = script,
                isTesseractBetaUsed = isBeta
            )
        } catch (e: Exception) {
            OcrResult(text = "", isTesseractBetaUsed = true)
        } finally {
            try {
                tessApi?.recycle()
            } catch (ignored: Exception) {}
        }
    }
}
