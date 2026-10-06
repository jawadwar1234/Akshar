package com.akshar.dyslexiareader.domain.util

/**
 * Text cleanup utility for OCR outputs:
 * 1. Fixes hyphenated line breaks (e.g. "con- \n tinue" -> "continue", "उदा-\nहरण" -> "उदाहरण").
 * 2. Collapses stray spaces & tabs while preserving proper word boundaries.
 * 3. Removes OCR noise characters (stray unreadable symbols, specks).
 * 4. Preserves paragraph breaks and Indic dandas (।, ॥).
 */
object TextCleanupUtil {

    // Regex for hyphen at the end of a line followed by optional space and newline, then word continuation
    // Using [\p{L}\p{M}]+ matches both Latin letters and Indic letters with combining marks/matras/viramas.
    private val HYPHENATED_LINE_BREAK_REGEX = Regex("([\\p{L}\\p{M}]+)[-\\u2010\\u2013]\\s*\\r?\\n\\s*([\\p{L}\\p{M}]+)")

    // Isolated OCR noise characters like standalone `~`, `^`, `|` (non-danda noise), `¬`, `§`
    private val OCR_NOISE_REGEX = Regex("(?:(?<=\\s)|^)[~^¬§¤±©®•★\uFFFD]+(?:(?=\\s)|$)")

    /**
     * Complete cleanup pipeline on raw OCR output.
     */
    fun cleanupOcrText(rawText: String): String {
        if (rawText.isBlank()) return ""

        var text = rawText

        // 1. Normalize carriage returns
        text = text.replace("\r\n", "\n").replace("\r", "\n")

        // 2. Fix hyphenated word breaks across lines
        text = fixHyphenatedLineBreaks(text)

        // 3. Remove OCR noise tokens
        text = removeNoiseCharacters(text)

        // 4. Preserve paragraph breaks while joining soft line-breaks inside paragraphs
        text = normalizeParagraphsAndLines(text)

        // 5. Collapse stray spaces and fix spacing around punctuation
        text = collapseStraySpaces(text)

        return text.trim()
    }

    /**
     * Fixes hyphenated words split across line breaks.
     * Works for English ("dis- \n cover" -> "discover") and Indic scripts ("उदा-\nहरण" -> "उदाहरण").
     */
    fun fixHyphenatedLineBreaks(text: String): String {
        return text.replace(HYPHENATED_LINE_BREAK_REGEX) { matchResult ->
            val firstPart = matchResult.groupValues[1]
            val secondPart = matchResult.groupValues[2]
            firstPart + secondPart
        }
    }

    /**
     * Removes OCR noise characters while strictly preserving Indic dandas (।, ॥),
     * quotes, punctuation, brackets, and alphanumeric tokens.
     */
    fun removeNoiseCharacters(text: String): String {
        var cleaned = text.replace(OCR_NOISE_REGEX, " ")

        // Remove non-printable control characters except \n and \t
        cleaned = cleaned.filter { c ->
            c == '\n' || c == '\t' || (!Character.isISOControl(c) && c != '\uFFFD')
        }

        return cleaned
    }

    /**
     * Normalizes line breaks:
     * - Double newlines (\n\n+) indicate paragraph boundaries and are preserved.
     * - Single newlines within continuous sentences are normalized with a space.
     */
    fun normalizeParagraphsAndLines(text: String): String {
        val paragraphs = text.split(Regex("\\n\\s*\\n+"))
        return paragraphs.joinToString("\n\n") { paragraph ->
            paragraph
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString(" ")
        }
    }

    /**
     * Collapses duplicate spaces and corrects spacing before punctuation marks.
     */
    fun collapseStraySpaces(text: String): String {
        var cleaned = text.replace(Regex("[ \\t]+"), " ")

        // Remove space before common punctuation: , . ! ? : ; ) ] } । ॥
        cleaned = cleaned.replace(Regex("\\s+([,.:;!?)\\]}।॥])"), "$1")

        // Ensure space after punctuation if followed by a letter/digit (and not a closing bracket or quote)
        cleaned = cleaned.replace(Regex("([,.:;!?)\\]}।॥])([\\p{L}\\p{M}\\p{N}])"), "$1 $2")

        return cleaned
    }
}
