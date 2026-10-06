package com.akshar.dyslexiareader.data.ocr

import android.graphics.Bitmap
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.ocr.OcrEngine
import com.akshar.dyslexiareader.domain.ocr.OcrLine
import com.akshar.dyslexiareader.domain.ocr.OcrPage
import com.akshar.dyslexiareader.domain.ocr.OcrResult
import com.akshar.dyslexiareader.domain.ocr.OcrTextBlock
import com.akshar.dyslexiareader.domain.ocr.OcrWord
import com.akshar.dyslexiareader.domain.util.ScriptDetector
import com.akshar.dyslexiareader.domain.util.TextCleanupUtil
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
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

        // Try Devanagari first (Devanagari model also recognizes digits & Latin text seamlessly)
        val devanagariResult = try {
            devanagariRecognizer.process(image).await()
        } catch (e: Exception) {
            null
        }

        if (devanagariResult != null && devanagariResult.text.isNotBlank()) {
            val script = ScriptDetector.detectScript(devanagariResult.text)
            val blocks = mapMlKitBlocks(devanagariResult)
            val fullRawText = buildPreservedText(blocks)
            val cleanedText = TextCleanupUtil.cleanupOcrText(fullRawText)

            val page = OcrPage(pageNumber = 1, blocks = blocks)

            return OcrResult(
                text = if (cleanedText.isNotBlank()) cleanedText else devanagariResult.text,
                pages = listOf(page),
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
            val blocks = mapMlKitBlocks(latinResult)
            val fullRawText = buildPreservedText(blocks)
            val cleanedText = TextCleanupUtil.cleanupOcrText(fullRawText)

            val page = OcrPage(pageNumber = 1, blocks = blocks)

            return OcrResult(
                text = if (cleanedText.isNotBlank()) cleanedText else latinResult.text,
                pages = listOf(page),
                blocks = blocks,
                detectedScript = SupportedScript.LATIN,
                isTesseractBetaUsed = false
            )
        }

        return OcrResult(text = "")
    }

    private fun mapMlKitBlocks(mlKitText: Text): List<OcrTextBlock> {
        return mlKitText.textBlocks.map { block ->
            val lines = block.lines.map { line ->
                val words = line.elements.map { element ->
                    OcrWord(
                        text = element.text,
                        boundingBox = element.boundingBox,
                        confidence = element.confidence
                    )
                }
                OcrLine(
                    text = line.text,
                    boundingBox = line.boundingBox,
                    words = words
                )
            }
            OcrTextBlock(
                text = block.text,
                boundingBox = block.boundingBox,
                lines = lines
            )
        }
    }

    private fun buildPreservedText(blocks: List<OcrTextBlock>): String {
        return blocks.joinToString("\n\n") { block ->
            block.lines.joinToString("\n") { it.text }
        }
    }
}
