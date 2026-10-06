package com.akshar.dyslexiareader.domain.ocr

import android.graphics.Rect
import com.akshar.dyslexiareader.domain.model.SupportedScript

data class OcrTextBlock(
    val text: String,
    val boundingBox: Rect?,
    val lines: List<String>
)

data class OcrResult(
    val text: String,
    val blocks: List<OcrTextBlock> = emptyList(),
    val detectedScript: SupportedScript = SupportedScript.LATIN,
    val isTesseractBetaUsed: Boolean = false
)
