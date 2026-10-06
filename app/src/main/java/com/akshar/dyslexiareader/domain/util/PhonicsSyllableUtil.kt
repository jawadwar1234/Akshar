package com.akshar.dyslexiareader.domain.util

import java.util.Locale

object PhonicsSyllableUtil {

    /**
     * Deconstructs a word into phonetic syllables / aksharas for easier dyslexia reading.
     * Preserves Indic conjuncts and vowel signs intact.
     */
    fun breakIntoSyllables(word: String, locale: Locale): List<String> {
        val clusters = UnicodeIndicTextUtil.extractGraphemeClusters(word, locale)
        if (clusters.isEmpty()) return emptyList()

        val isLatin = locale.language.equals("en", ignoreCase = true)
        if (isLatin) {
            return breakLatinSyllables(word)
        }

        // For Indic scripts (Devanagari, Gujarati, Bengali), grapheme clusters (Aksharas)
        // already form the natural phonetic reading units (उदा. नम-स्ते -> न, म, स्ते).
        return clusters
    }

    /**
     * Rule-based English syllabification.
     */
    private fun breakLatinSyllables(word: String): List<String> {
        val lower = word.lowercase()
        if (lower.length <= 3) return listOf(word)

        val vowels = "aeiouy"
        val syllables = mutableListOf<String>()
        var currentSyllable = StringBuilder()

        var i = 0
        while (i < word.length) {
            currentSyllable.append(word[i])
            val isVowel = vowels.contains(lower[i])

            if (isVowel && i < word.length - 2) {
                val nextChar = lower[i + 1]
                val nextNextChar = lower[i + 2]
                // If followed by consonant then vowel, break here (e.g., pa-per)
                if (!vowels.contains(nextChar) && vowels.contains(nextNextChar)) {
                    syllables.add(currentSyllable.toString())
                    currentSyllable = StringBuilder()
                } else if (!vowels.contains(nextChar) && !vowels.contains(nextNextChar) && i < word.length - 3) {
                    // Double consonant break (e.g., let-ter)
                    currentSyllable.append(word[i + 1])
                    syllables.add(currentSyllable.toString())
                    currentSyllable = StringBuilder()
                    i++
                }
            }
            i++
        }

        if (currentSyllable.isNotEmpty()) {
            if (syllables.isNotEmpty() && currentSyllable.length == 1 && !vowels.contains(currentSyllable[0].lowercaseChar())) {
                val last = syllables.removeAt(syllables.size - 1)
                syllables.add(last + currentSyllable)
            } else {
                syllables.add(currentSyllable.toString())
            }
        }

        return if (syllables.isEmpty()) listOf(word) else syllables
    }

    /**
     * Splits a word into (fixationPrefix, suffix) for Bionic reading guidance.
     * Highlights initial 40-50% of grapheme clusters.
     */
    fun splitForBionicFocus(word: String, locale: Locale): Pair<String, String> {
        val clusters = UnicodeIndicTextUtil.extractGraphemeClusters(word, locale)
        if (clusters.size <= 1) return Pair(word, "")

        val highlightCount = when (clusters.size) {
            2, 3 -> 1
            4, 5 -> 2
            6, 7 -> 3
            else -> (clusters.size * 0.45).toInt().coerceAtLeast(1)
        }

        val prefix = clusters.take(highlightCount).joinToString("")
        val suffix = clusters.drop(highlightCount).joinToString("")
        return Pair(prefix, suffix)
    }
}
