package com.akshar.dyslexiareader.domain.util

import kotlin.math.min

object WordMatcher {

    /**
     * Compares a recognized spoken word/phrase against a target word with phonetic/accent tolerance.
     * Returns true if spoken input matches or is close enough (Levenshtein distance <= threshold).
     */
    fun isFuzzyMatch(spokenInput: String, targetWord: String): Boolean {
        val cleanSpoken = UnicodeIndicTextUtil.cleanWordForMatching(spokenInput)
        val cleanTarget = UnicodeIndicTextUtil.cleanWordForMatching(targetWord)

        if (cleanSpoken == cleanTarget) return true
        if (cleanSpoken.contains(cleanTarget) || cleanTarget.contains(cleanSpoken)) return true

        // Check Levenshtein distance on grapheme clusters
        val spokenClusters = UnicodeIndicTextUtil.extractGraphemeClusters(cleanSpoken)
        val targetClusters = UnicodeIndicTextUtil.extractGraphemeClusters(cleanTarget)

        val distance = computeLevenshteinDistance(spokenClusters, targetClusters)
        val maxLen = maxOf(spokenClusters.size, targetClusters.size)

        // For short words (<= 3 clusters), require exact or 1 edit max. For longer words, allow up to 30% error
        val allowedDistance = when {
            maxLen <= 3 -> 1
            maxLen <= 6 -> 2
            else -> (maxLen * 0.35).toInt().coerceAtLeast(2)
        }

        return distance <= allowedDistance
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
