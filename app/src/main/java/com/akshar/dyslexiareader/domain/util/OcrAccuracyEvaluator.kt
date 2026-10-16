package com.akshar.dyslexiareader.domain.util

import kotlin.math.min

data class OcrAccuracyReport(
    val language: String,
    val groundTruthLength: Int,
    val extractedTextLength: Int,
    val characterErrors: Int,
    val wordErrors: Int,
    val totalWords: Int,
    val characterAccuracyPercent: Double,
    val wordAccuracyPercent: Double,
    val characterErrorRate: Double,
    val wordErrorRate: Double
)

object OcrAccuracyEvaluator {

    /**
     * Evaluates OCR accuracy between ground truth and extracted text.
     * Computes Character Error Rate (CER), Word Error Rate (WER), and accuracy percentage.
     */
    fun evaluate(groundTruth: String, extractedText: String, language: String = "unknown"): OcrAccuracyReport {
        val normTruth = TextCleanupUtil.cleanupOcrText(groundTruth)
        val normExtracted = TextCleanupUtil.cleanupOcrText(extractedText)

        val charDistance = calculateLevenshteinDistance(normTruth, normExtracted)
        val cer = if (normTruth.isNotEmpty()) {
            charDistance.toDouble() / normTruth.length
        } else {
            0.0
        }
        val charAccuracy = ((1.0 - cer).coerceIn(0.0, 1.0)) * 100.0

        val truthWords = normTruth.split(Regex("\\s+")).filter { it.isNotBlank() }
        val extractedWords = normExtracted.split(Regex("\\s+")).filter { it.isNotBlank() }

        val wordDistance = calculateWordLevenshteinDistance(truthWords, extractedWords)
        val wer = if (truthWords.isNotEmpty()) {
            wordDistance.toDouble() / truthWords.size
        } else {
            0.0
        }
        val wordAccuracy = ((1.0 - wer).coerceIn(0.0, 1.0)) * 100.0

        return OcrAccuracyReport(
            language = language,
            groundTruthLength = normTruth.length,
            extractedTextLength = normExtracted.length,
            characterErrors = charDistance,
            wordErrors = wordDistance,
            totalWords = truthWords.size,
            characterAccuracyPercent = charAccuracy,
            wordAccuracyPercent = wordAccuracy,
            characterErrorRate = cer * 100.0,
            wordErrorRate = wer * 100.0
        )
    }

    /**
     * Computes classic Levenshtein edit distance at the character level.
     */
    fun calculateLevenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        var prev = IntArray(n + 1) { it }
        var curr = IntArray(n + 1)

        for (i in 1..m) {
            curr[0] = i
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                curr[j] = min(
                    min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                )
            }
            val temp = prev
            prev = curr
            curr = temp
        }
        return prev[n]
    }

    /**
     * Computes Levenshtein edit distance at the word level for Word Error Rate (WER).
     */
    fun calculateWordLevenshteinDistance(w1: List<String>, w2: List<String>): Int {
        val m = w1.size
        val n = w2.size
        var prev = IntArray(n + 1) { it }
        var curr = IntArray(n + 1)

        for (i in 1..m) {
            curr[0] = i
            for (j in 1..n) {
                val cost = if (w1[i - 1] == w2[j - 1]) 0 else 1
                curr[j] = min(
                    min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                )
            }
            val temp = prev
            prev = curr
            curr = temp
        }
        return prev[n]
    }
}
