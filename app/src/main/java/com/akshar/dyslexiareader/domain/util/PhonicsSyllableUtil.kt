package com.akshar.dyslexiareader.domain.util

import androidx.compose.ui.graphics.Color
import java.util.Locale

object PhonicsSyllableUtil {

    // 3 Curated Color-Blind-Safe Palettes (Soft, high-contrast, accessible)
    val COLOR_BLIND_PALETTES: List<Pair<Color, Color>> = listOf(
        Pair(Color(0xFF1D4ED8), Color(0xFFB45309)), // 0: Slate Blue & Warm Amber
        Pair(Color(0xFF15803D), Color(0xFF7E22CE)), // 1: Forest Sage & Deep Plum
        Pair(Color(0xFF0F766E), Color(0xFFC2410C))  // 2: Deep Teal & Terracotta Rust
    )

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

        // For Indic scripts (Devanagari, Gujarati, Bengali), group grapheme clusters 2 to 3 at a time
        return groupIndicClusters(clusters)
    }

    /**
     * Groups Indic grapheme clusters into 2-3 cluster chunks without ever breaking inside a cluster.
     */
    fun groupIndicClusters(clusters: List<String>): List<String> {
        if (clusters.size <= 2) return clusters

        val chunks = mutableListOf<String>()
        var i = 0
        while (i < clusters.size) {
            val remaining = clusters.size - i
            if (remaining == 4) {
                // Split 4 into 2 + 2 for balance
                chunks.add(clusters[i] + clusters[i + 1])
                chunks.add(clusters[i + 2] + clusters[i + 3])
                break
            } else if (remaining >= 3) {
                // Group 3 clusters
                chunks.add(clusters[i] + clusters[i + 1] + clusters[i + 2])
                i += 3
            } else if (remaining == 2) {
                chunks.add(clusters[i] + clusters[i + 1])
                i += 2
            } else {
                // Attach single trailing cluster to previous chunk if available
                if (chunks.isNotEmpty()) {
                    val last = chunks.removeAt(chunks.size - 1)
                    chunks.add(last + clusters[i])
                } else {
                    chunks.add(clusters[i])
                }
                i += 1
            }
        }
        return chunks
    }

    /**
     * Rule-based English vowel-group syllabification.
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

    fun getChunkPalette(paletteIndex: Int): Pair<Color, Color> {
        val idx = paletteIndex.coerceIn(0, COLOR_BLIND_PALETTES.size - 1)
        return COLOR_BLIND_PALETTES[idx]
    }
}
