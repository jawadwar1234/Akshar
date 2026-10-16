package com.akshar.dyslexiareader.data.ocr

import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.util.OcrAccuracyEvaluator
import com.akshar.dyslexiareader.domain.util.ScriptDetector
import com.akshar.dyslexiareader.domain.util.TextCleanupUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OcrAccuracyTest {

    private fun loadTestDataOrFallback(filename: String, fallback: String): String {
        val candidatePaths = listOf(
            File("testdata", filename),
            File("../testdata", filename),
            File("../../testdata", filename)
        )
        for (path in candidatePaths) {
            if (path.exists()) {
                return path.readText().trim()
            }
        }
        return fallback.trim()
    }

    @Test
    fun `evaluate Gujarati OCR text accuracy and cleanup pipeline on testdata`() {
        val groundTruth = loadTestDataOrFallback(
            "gujarati_sample_1.txt",
            "નિયમિત વાંચનથી શબ્દભંડોળ વધે છે અને સમજવાની શક્તિ ખીલે છે. પુસ્તકો આપણા સાચા મિત્રો છે."
        )

        // Simulate typical OCR output with minor noise and line hyphenation
        val simulatedOcrOutput = groundTruth
            .replace("શબ્દભંડોળ", "શબ્દ-\nભંડોળ") // hyphenated line break
            .plus(" ~") // OCR noise symbol

        val cleaned = TextCleanupUtil.cleanupOcrText(simulatedOcrOutput)
        val report = OcrAccuracyEvaluator.evaluate(groundTruth, cleaned, "Gujarati")

        val detectedScript = ScriptDetector.detectScript(cleaned)
        assertEquals("Should detect Gujarati script", SupportedScript.GUJARATI, detectedScript)

        assertTrue("Character accuracy should exceed 95%", report.characterAccuracyPercent >= 95.0)
        assertTrue("CER should be below 5%", report.characterErrorRate <= 5.0)
        assertTrue("Word accuracy should exceed 95%", report.wordAccuracyPercent >= 95.0)
    }

    @Test
    fun `evaluate Bengali OCR text accuracy and cleanup pipeline on testdata`() {
        val groundTruth = loadTestDataOrFallback(
            "bengali_sample_1.txt",
            "নিয়মিত বই পড়লে আমাদের কল্পনাশক্তির বিকাশ ঘটে এবং নতুন নতুন শব্দ শেখা যায়। বই মানুষের শ্রেষ্ঠ সঙ্গী ও পথপ্রদর্শক।"
        )

        // Simulate typical OCR output with soft line break and noise
        val simulatedOcrOutput = groundTruth
            .replace("কল্পনাশক্তির", "কল্পনা-\nশক্তির")
            .plus(" ^")

        val cleaned = TextCleanupUtil.cleanupOcrText(simulatedOcrOutput)
        val report = OcrAccuracyEvaluator.evaluate(groundTruth, cleaned, "Bengali")

        val detectedScript = ScriptDetector.detectScript(cleaned)
        assertEquals("Should detect Bengali script", SupportedScript.BENGALI, detectedScript)

        assertTrue("Character accuracy should exceed 95%", report.characterAccuracyPercent >= 95.0)
        assertTrue("CER should be below 5%", report.characterErrorRate <= 5.0)
        assertTrue("Word accuracy should exceed 95%", report.wordAccuracyPercent >= 95.0)
    }

    @Test
    fun `evaluate Hindi and English OCR text accuracy on testdata`() {
        val hindiGroundTruth = loadTestDataOrFallback(
            "hindi_sample_1.txt",
            "किताबें ज्ञान का सबसे बड़ा स्रोत हैं। नियमित रूप से पढ़ाई करने से हमारी भाषा में सुधार होता है।"
        )
        val englishGroundTruth = loadTestDataOrFallback(
            "english_sample_1.txt",
            "Reading every day improves our vocabulary and strengthens imagination. Books are true companions."
        )

        val hindiClean = TextCleanupUtil.cleanupOcrText(hindiGroundTruth.replace("किताबें", "किता-\nबें"))
        val englishClean = TextCleanupUtil.cleanupOcrText(englishGroundTruth.replace("vocabulary", "voca-\nbulary"))

        val hindiReport = OcrAccuracyEvaluator.evaluate(hindiGroundTruth, hindiClean, "Hindi")
        val englishReport = OcrAccuracyEvaluator.evaluate(englishGroundTruth, englishClean, "English")

        assertEquals(SupportedScript.DEVANAGARI, ScriptDetector.detectScript(hindiClean))
        assertEquals(SupportedScript.LATIN, ScriptDetector.detectScript(englishClean))

        assertTrue("Hindi accuracy should be 100% after cleanup", hindiReport.characterAccuracyPercent == 100.0)
        assertTrue("English accuracy should be 100% after cleanup", englishReport.characterAccuracyPercent == 100.0)
    }

    @Test
    fun `verify Levenshtein distance calculations on character and word levels`() {
        val dist = OcrAccuracyEvaluator.calculateLevenshteinDistance("kitten", "sitting")
        assertEquals(3, dist)

        val wordDist = OcrAccuracyEvaluator.calculateWordLevenshteinDistance(
            listOf("the", "quick", "brown", "fox"),
            listOf("the", "fast", "brown", "fox")
        )
        assertEquals(1, wordDist)
    }
}
