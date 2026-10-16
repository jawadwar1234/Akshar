package com.akshar.dyslexiareader.data.ocr

import android.graphics.Bitmap
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.ocr.OcrEngine
import com.akshar.dyslexiareader.domain.ocr.OcrResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositeOcrEngine @Inject constructor(
    private val mlKitOcrEngine: MlKitOcrEngine,
    private val tesseractOcrEngine: TesseractOcrEngine
) : OcrEngine {

    override suspend fun recognizeText(bitmap: Bitmap): OcrResult = recognizeText(bitmap, null)

    suspend fun recognizeText(bitmap: Bitmap, preferredScript: SupportedScript? = null): OcrResult {
        // If user specifically requested Gujarati or Bengali, route directly to Tesseract
        if (preferredScript == SupportedScript.GUJARATI) {
            val res = tesseractOcrEngine.recognizeText(bitmap, "guj")
            if (res.text.isNotBlank()) return res
        } else if (preferredScript == SupportedScript.BENGALI) {
            val res = tesseractOcrEngine.recognizeText(bitmap, "ben")
            if (res.text.isNotBlank()) return res
        }

        // 1. Run high-speed offline ML Kit engine (Latin & Devanagari)
        val mlKitResult = mlKitOcrEngine.recognizeText(bitmap)
        if (mlKitResult.text.isNotBlank()) {
            return mlKitResult
        }

        // 2. If ML Kit returned empty, attempt Tesseract fallback
        val tesseractResult = tesseractOcrEngine.recognizeText(bitmap)
        if (tesseractResult.text.isNotBlank()) {
            return tesseractResult
        }

        return mlKitResult
    }
}
