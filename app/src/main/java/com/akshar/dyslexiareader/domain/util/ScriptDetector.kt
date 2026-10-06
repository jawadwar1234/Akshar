package com.akshar.dyslexiareader.domain.util

import com.akshar.dyslexiareader.domain.model.SupportedScript

object ScriptDetector {

    /**
     * Detects the dominant script in the given input text by counting code points in Unicode blocks.
     */
    fun detectScript(text: String): SupportedScript {
        if (text.isBlank()) return SupportedScript.LATIN

        var devanagariCount = 0
        var gujaratiCount = 0
        var bengaliCount = 0
        var latinCount = 0

        var index = 0
        while (index < text.length) {
            val codePoint = text.codePointAt(index)
            when (codePoint) {
                // Devanagari block: U+0900 - U+097F
                in 0x0900..0x097F -> devanagariCount++
                // Gujarati block: U+0A80 - U+0AFF
                in 0x0A80..0x0AFF -> gujaratiCount++
                // Bengali block: U+0980 - U+09FF
                in 0x0980..0x09FF -> bengaliCount++
                // Latin block (Basic Latin & Extended): A-Z, a-z
                in 0x0041..0x005A, in 0x0061..0x007A, in 0x00C0..0x024F -> latinCount++
            }
            index += Character.charCount(codePoint)
        }

        val maxCount = maxOf(devanagariCount, gujaratiCount, bengaliCount, latinCount)
        if (maxCount == 0) return SupportedScript.LATIN

        return when (maxCount) {
            devanagariCount -> SupportedScript.DEVANAGARI
            gujaratiCount -> SupportedScript.GUJARATI
            bengaliCount -> SupportedScript.BENGALI
            else -> SupportedScript.LATIN
        }
    }
}
