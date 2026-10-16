package com.akshar.dyslexiareader.domain.util

import kotlin.math.min

object WordMatcher {

    /**
     * Compares a recognized spoken word/phrase against a target word with phonetic/accent tolerance.
     * Returns true if spoken input matches or is close enough.
     *
     * Handles:
     * - Case insensitivity & punctuation/whitespace stripping
     * - English phonetic simplifications (ph->f, kn->n, wr->r, double consonants, -ing->-in)
     * - Hindi/Indic nuqta normalization (ज़->ज, फ़->फ, ड़->ड, etc.)
     * - Hindi/Indic anusvara / chandrabindu / nasal conjunct equivalence (हिंदी <-> हिन्दी)
     * - Hindi/Indic short vs long vowel matra tolerance (किताब <-> कीताब, सूरज <-> सुरज)
     * - Hindi/Indic speech recognizer conjunct & schwa split tolerance (प्रकाश <-> परकाश, पुस्तक <-> पुसतक, ज्ञान <-> ग्यान)
     * - Grapheme cluster Levenshtein distance with forgiving adaptive thresholds
     */
    fun isFuzzyMatch(spokenInput: String, targetWord: String): Boolean {
        val cleanSpoken = UnicodeIndicTextUtil.cleanWordForMatching(spokenInput)
        val cleanTarget = UnicodeIndicTextUtil.cleanWordForMatching(targetWord)

        if (cleanSpoken.isBlank() || cleanTarget.isBlank()) return false

        // 1. Direct match or substring containment
        if (cleanSpoken == cleanTarget) return true
        if (cleanSpoken.contains(cleanTarget) || cleanTarget.contains(cleanSpoken)) return true

        // 2. Phonetic normalized exact match
        val phonSpoken = normalizePhonetics(cleanSpoken)
        val phonTarget = normalizePhonetics(cleanTarget)

        if (phonSpoken == phonTarget) return true
        if (phonSpoken.contains(phonTarget) || phonTarget.contains(phonSpoken)) return true

        // 3. Indic Conjunct & Schwa Skeleton match (Devanagari / Indic)
        if (isIndicScript(cleanTarget)) {
            val skelSpoken = indicConsonantSkeleton(cleanSpoken)
            val skelTarget = indicConsonantSkeleton(cleanTarget)
            if (skelSpoken == skelTarget && skelTarget.length >= 2) return true
            if (skelSpoken.contains(skelTarget) || skelTarget.contains(skelSpoken)) return true
        }

        // 4. Check Levenshtein distance on grapheme clusters (phonetically normalized)
        val spokenClusters = UnicodeIndicTextUtil.extractGraphemeClusters(phonSpoken)
        val targetClusters = UnicodeIndicTextUtil.extractGraphemeClusters(phonTarget)

        val distance = computeLevenshteinDistance(spokenClusters, targetClusters)
        val maxLen = maxOf(spokenClusters.size, targetClusters.size)

        // For short words (<= 3 clusters), allow 1 edit. For medium words (<= 6 clusters), allow 2 edits.
        // For longer words, allow up to 35% error.
        val allowedDistance = when {
            maxLen <= 3 -> 1
            maxLen <= 6 -> 2
            else -> (maxLen * 0.35).toInt().coerceAtLeast(2)
        }

        if (distance <= allowedDistance) return true

        // 5. Fallback raw grapheme cluster distance
        val rawSpokenClusters = UnicodeIndicTextUtil.extractGraphemeClusters(cleanSpoken)
        val rawTargetClusters = UnicodeIndicTextUtil.extractGraphemeClusters(cleanTarget)
        val rawDistance = computeLevenshteinDistance(rawSpokenClusters, rawTargetClusters)
        val rawMaxLen = maxOf(rawSpokenClusters.size, rawTargetClusters.size)
        val rawAllowedDistance = when {
            rawMaxLen <= 3 -> 1
            rawMaxLen <= 6 -> 2
            else -> (rawMaxLen * 0.35).toInt().coerceAtLeast(2)
        }

        return rawDistance <= rawAllowedDistance
    }

    /**
     * Checks if spoken speech transcription matches any word in a target sentence.
     */
    fun findMatchedWordsInSentence(spokenTranscription: String, targetWords: List<String>): Set<String> {
        val spokenWords = UnicodeIndicTextUtil.extractWordSpans(spokenTranscription).map { it.cleanWord }
        val matched = mutableSetOf<String>()

        for (target in targetWords) {
            val cleanTarget = UnicodeIndicTextUtil.cleanWordForMatching(target)
            for (spoken in spokenWords) {
                if (isFuzzyMatch(spoken, cleanTarget)) {
                    matched.add(target)
                    break
                }
            }
        }
        return matched
    }

    /**
     * Normalizes text phonetically for English and Indic scripts.
     */
    fun normalizePhonetics(text: String): String {
        var s = text.trim()

        // Strip zero-width characters
        s = s.replace("\u200B", "") // ZWSP
            .replace("\u200C", "") // ZWNJ
            .replace("\u200D", "") // ZWJ

        if (isIndicScript(s)) {
            // --- HINDI / DEVANAGARI & INDIC NORMALIZATION ---
            // 1. Nuqta normalization
            s = s.replace("\u0958", "क") // क़ -> क
                .replace("\u0959", "ख") // ख़ -> ख
                .replace("\u095A", "ग") // ग़ -> ग
                .replace("\u095B", "ज") // ज़ -> ज
                .replace("\u095C", "ड") // ड़ -> ड
                .replace("\u095D", "ढ") // ढ़ -> ढ
                .replace("\u095E", "फ") // फ़ -> फ
                .replace("\u095F", "य") // य़ -> य
                .replace("\u093C", "")  // generic Devanagari nuqta strip

            // 2. Anusvara & Chandrabindu equivalence
            s = s.replace("\u0901", "\u0902") // Chandrabindu -> Anusvara

            // 3. Nasal conjuncts to Anusvara (e.g., न्द -> ंद, न्त -> ंत, म्ब -> ंब)
            s = s.replace("न्", "\u0902")
                .replace("म्", "\u0902")
                .replace("ङ्", "\u0902")
                .replace("ञ्", "\u0902")
                .replace("ण्", "\u0902")

            // 4. Short vs Long vowel matra & independent vowel equivalence
            s = s.replace("ी", "ि")
                .replace("ू", "ु")
                .replace("ै", "े")
                .replace("ौ", "ो")
                .replace("ई", "इ")
                .replace("ऊ", "उ")
                .replace("ऐ", "ए")
                .replace("औ", "ओ")

            // 5. Common speech recognizer Indic phoneme substitutes
            s = s.replace("ज्ञान", "ग्यान")
                .replace("ज्ञ", "ग्य")
                .replace("ऋ", "रि")
                .replace("ृ", "्र")
                .replace("ष", "श")
                .replace("स", "श")
        } else {
            // --- ENGLISH / LATIN PHONETIC NORMALIZATION ---
            s = s.lowercase()

            // 1. Digraph & silent letter simplifications
            if (s.startsWith("kn")) s = "n" + s.substring(2)
            if (s.startsWith("wr")) s = "r" + s.substring(2)
            if (s.startsWith("wh")) s = "w" + s.substring(2)
            if (s.startsWith("ps")) s = "s" + s.substring(2)

            s = s.replace("ph", "f")
                .replace("ck", "k")
                .replace("qu", "kw")
                .replace("tion", "shun")
                .replace("sion", "shun")
                .replace("ough", "of")
                .replace("igh", "y")
                .replace("ght", "t")

            // 2. Trailing -ing vs -in tolerance (reading <-> readin)
            if (s.endsWith("ing")) {
                s = s.substring(0, s.length - 3) + "in"
            }

            // 3. Collapse consecutive duplicate letters (e.g. butterfly -> buterfly, running -> runin)
            val collapsed = StringBuilder()
            var prevChar: Char? = null
            for (c in s) {
                if (c != prevChar) {
                    collapsed.append(c)
                    prevChar = c
                }
            }
            s = collapsed.toString()
        }

        return s
    }

    /**
     * Extracts consonant skeleton for Indic words to tolerate halants / split conjuncts / schwa deletion.
     * e.g. प्रकाश -> परकश -> प्रकश, पुस्तक -> पुसतक -> पस्तक
     */
    private fun indicConsonantSkeleton(word: String): String {
        // Strip all vowel signs (matras: \u093E-\u094C, \u0962-\u0963) and virama (\u094D) and anusvara (\u0902)
        val cleaned = normalizePhonetics(word)
        return cleaned.replace(Regex("[\u093E-\u094D\u0901-\u0903\u0962-\u0963]"), "")
    }

    private fun isIndicScript(text: String): Boolean {
        for (c in text) {
            val block = Character.UnicodeBlock.of(c)
            if (block == Character.UnicodeBlock.DEVANAGARI ||
                block == Character.UnicodeBlock.GUJARATI ||
                block == Character.UnicodeBlock.BENGALI
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Generic Levenshtein distance on lists of grapheme clusters.
     */
    fun <T> computeLevenshteinDistance(lhs: List<T>, rhs: List<T>): Int {
        val lhsLen = lhs.size
        val rhsLen = rhs.size

        var cost = IntArray(lhsLen + 1) { it }
        var newCost = IntArray(lhsLen + 1)

        for (i in 1..rhsLen) {
            newCost[0] = i
            for (j in 1..lhsLen) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLen]
    }
}

