package com.akshar.dyslexiareader.domain.util

import com.akshar.dyslexiareader.domain.model.FormattedReaderText
import com.akshar.dyslexiareader.domain.model.SentenceSpan
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.model.WordSpan
import java.text.BreakIterator
import java.util.Locale

object UnicodeIndicTextUtil {

    // Indic Virama / Halant code points for Devanagari, Gujarati, Bengali
    private val INDIC_VIRAMAS = setOf('\u094D', '\u0ACD', '\u09CD')

    /**
     * Splits text into true Unicode grapheme clusters / Indic orthographic syllables (Aksharas).
     * Never slices dependent matras or conjunct viramas (उदा. स्ते, ज्ञ, क्ष, ષ્ટ, স্ক).
     */
    fun extractGraphemeClusters(text: String, locale: Locale = Locale.ROOT): List<String> {
        if (text.isEmpty()) return emptyList()
        val rawClusters = mutableListOf<String>()
        val boundary = BreakIterator.getCharacterInstance(locale)
        boundary.setText(text)

        var start = boundary.first()
        var end = boundary.next()
        while (end != BreakIterator.DONE) {
            rawClusters.add(text.substring(start, end))
            start = end
            end = boundary.next()
        }

        // Merge Virama-connected and Matra-combining clusters so Indic conjuncts and vowel signs remain intact
        val merged = mutableListOf<String>()
        var idx = 0
        while (idx < rawClusters.size) {
            var current = rawClusters[idx]
            while (idx + 1 < rawClusters.size &&
                (INDIC_VIRAMAS.contains(current.lastOrNull()) ||
                 (rawClusters[idx + 1].isNotEmpty() && isCombiningOrMatra(rawClusters[idx + 1].first())))
            ) {
                idx++
                current += rawClusters[idx]
            }
            merged.add(current)
            idx++
        }
        return merged
    }

    private fun isCombiningOrMatra(c: Char): Boolean {
        if (INDIC_VIRAMAS.contains(c)) return true
        val type = Character.getType(c)
        return type == Character.NON_SPACING_MARK.toInt() ||
               type == Character.COMBINING_SPACING_MARK.toInt() ||
               type == Character.ENCLOSING_MARK.toInt()
    }

    /**
     * Splits text into word spans using Unicode BreakIterator word boundaries.
     */
    fun extractWordSpans(text: String, locale: Locale = Locale.ROOT): List<WordSpan> {
        if (text.isBlank()) return emptyList()
        val wordSpans = mutableListOf<WordSpan>()
        val boundary = BreakIterator.getWordInstance(locale)
        boundary.setText(text)

        var start = boundary.first()
        var end = boundary.next()
        while (end != BreakIterator.DONE) {
            val candidate = text.substring(start, end)
            // Filter out pure whitespace / punctuation tokens
            if (isWordToken(candidate)) {
                val clean = cleanWordForMatching(candidate)
                val syllables = PhonicsSyllableUtil.breakIntoSyllables(clean, locale)
                wordSpans.add(
                    WordSpan(
                        word = candidate,
                        startIndex = start,
                        endIndex = end,
                        cleanWord = clean,
                        syllables = syllables
                    )
                )
            }
            start = end
            end = boundary.next()
        }
        return wordSpans
    }

    /**
     * Splits text into sentence spans using Unicode BreakIterator sentence boundaries.
     */
    fun extractSentenceSpans(text: String, locale: Locale = Locale.ROOT): List<SentenceSpan> {
        if (text.isBlank()) return emptyList()
        val sentenceSpans = mutableListOf<SentenceSpan>()
        val boundary = BreakIterator.getSentenceInstance(locale)
        boundary.setText(text)

        var start = boundary.first()
        var end = boundary.next()
        while (end != BreakIterator.DONE) {
            val sentenceStr = text.substring(start, end).trim()
            if (sentenceStr.isNotEmpty()) {
                val words = extractWordSpans(sentenceStr, locale).map { w ->
                    // Adjust word offsets relative to the full text
                    val relativeStart = text.indexOf(w.word, start)
                    val actualStart = if (relativeStart != -1) relativeStart else start + w.startIndex
                    w.copy(
                        startIndex = actualStart,
                        endIndex = actualStart + w.word.length
                    )
                }
                sentenceSpans.add(
                    SentenceSpan(
                        sentence = sentenceStr,
                        startIndex = start,
                        endIndex = end,
                        words = words
                    )
                )
            }
            start = end
            end = boundary.next()
        }
        return sentenceSpans
    }

    /**
     * Parses full text into a structured reader model with detected script and boundary mappings.
     */
    fun parseReaderText(rawText: String): FormattedReaderText {
        val clean = normalizeText(rawText)
        val script = ScriptDetector.detectScript(clean)
        val locale = script.primaryLocale
        val sentences = extractSentenceSpans(clean, locale)
        val allWords = extractWordSpans(clean, locale)

        return FormattedReaderText(
            fullText = clean,
            script = script,
            sentences = sentences,
            allWords = allWords
        )
    }

    /**
     * Cleans up OCR artifacts, excessive newlines, and normalizes spaces while preserving Indic characters.
     */
    fun normalizeText(text: String): String {
        return text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    /**
     * Checks if a token contains at least one letter/digit character.
     */
    private fun isWordToken(token: String): Boolean {
        var i = 0
        while (i < token.length) {
            val cp = token.codePointAt(i)
            if (Character.isLetterOrDigit(cp)) return true
            i += Character.charCount(cp)
        }
        return false
    }

    /**
     * Strips punctuation, commas, danda (|), double danda (||) for phonetic comparison and practice mode.
     */
    fun cleanWordForMatching(word: String): String {
        return word
            .replace(Regex("[.,!?;:\"'()\\-\\[\\]{}|।॥‘’“”]"), "")
            .trim()
            .lowercase()
    }
}
