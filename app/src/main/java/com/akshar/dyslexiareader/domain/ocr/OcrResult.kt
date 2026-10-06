package com.akshar.dyslexiareader.domain.ocr

import android.graphics.Rect
import com.akshar.dyslexiareader.domain.model.SupportedScript

data class OcrWord(
    val text: String,
    val boundingBox: Rect?,
    val confidence: Float? = null
)

data class OcrLine(
    val text: String,
    val boundingBox: Rect?,
    val words: List<OcrWord> = emptyList()
)

data class OcrTextBlock(
    val text: String,
    val boundingBox: Rect?,
    val lines: List<OcrLine> = emptyList()
)

data class OcrPage(
    val pageNumber: Int,
    val blocks: List<OcrTextBlock> = emptyList(),
    val imagePath: String? = null
)

data class OcrResult(
    val text: String,
    val pages: List<OcrPage> = emptyList(),
    val blocks: List<OcrTextBlock> = emptyList(),
    val detectedScript: SupportedScript = SupportedScript.LATIN,
    val isTesseractBetaUsed: Boolean = false
)
