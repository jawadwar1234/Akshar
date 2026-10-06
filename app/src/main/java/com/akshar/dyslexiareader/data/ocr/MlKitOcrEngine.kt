package com.akshar.dyslexiareader.data.ocr

import android.graphics.Bitmap
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.ocr.OcrEngine
import com.akshar.dyslexiareader.domain.ocr.OcrResult
import com.akshar.dyslexiareader.domain.ocr.OcrTextBlock
import com.akshar.dyslexiareader.domain.util.ScriptDetector
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitOcrEngine @Inject constructor() : OcrEngine {

    // Bundled offline Latin Recognizer
    private val latinRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    // Bundled offline Devanagari (Hindi / Marathi) Recognizer
    private val devanagariRecognizer by lazy {
        TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
    }

    override suspend fun recognizeText(bitmap: Bitmap): OcrResult {
        val image = InputImage.fromBitmap(bitmap, 0)

        // Try Devanagari first (Devanagari model also recognizes Latin numbers & Latin words seamlessly)
        val devanagariResult = try {
            devanagariRecognizer.process(image).await()
        } catch (e: Exception) {
            null
        }

        if (devanagariResult != null && devanagariResult.text.isNotBlank()) {
            val script = ScriptDetector.detectScript(devanagariResult.text)
            val blocks = devanagariResult.textBlocks.map { block ->
                OcrTextBlock(
                    text = block.text,
                    boundingBox = block.boundingBox,
                    lines = block.lines.map { it.text }
                )
            }
            return OcrResult(
                text = devanagariResult.text,
                blocks = blocks,
                detectedScript = script,
                isTesseractBetaUsed = false
            )
        }

        // Fallback to standard Latin recognizer
        val latinResult = try {
            latinRecognizer.process(image).await()
        } catch (e: Exception) {
            null
        }

        if (latinResult != null && latinResult.text.isNotBlank()) {
            val blocks = latinResult.textBlocks.map { block ->
                OcrTextBlock(
                    text = block.text,
                    boundingBox = block.boundingBox,
                    lines = block.lines.map { it.text }
                )
            }
            return OcrResult(
                text = latinResult.text,
                blocks = blocks,
                detectedScript = SupportedScript.LATIN,
                isTesseractBetaUsed = false
            )
        }

        return OcrResult(text = "")
    }
}
