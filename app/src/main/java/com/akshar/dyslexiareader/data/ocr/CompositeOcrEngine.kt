package com.akshar.dyslexiareader.data.ocr

import android.graphics.Bitmap
import com.akshar.dyslexiareader.domain.ocr.OcrEngine
import com.akshar.dyslexiareader.domain.ocr.OcrResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositeOcrEngine @Inject constructor(
    private val mlKitOcrEngine: MlKitOcrEngine,
    private val tesseractOcrEngine: TesseractOcrEngine
) : OcrEngine {

    override suspend fun recognizeText(bitmap: Bitmap): OcrResult {
        // 1. First run high-speed offline ML Kit engine (Latin & Devanagari)
        val mlKitResult = mlKitOcrEngine.recognizeText(bitmap)
        if (mlKitResult.text.isNotBlank()) {
            return mlKitResult
        }

        // 2. If ML Kit returned empty or low confidence, attempt Tesseract (Beta for Gujarati & Bengali)
        val tesseractResult = tesseractOcrEngine.recognizeText(bitmap)
        if (tesseractResult.text.isNotBlank()) {
            return tesseractResult
        }

        return mlKitResult
    }
}
