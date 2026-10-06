package com.akshar.dyslexiareader.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextCleanupUtilTest {

    @Test
    fun `fixHyphenatedLineBreaks merges English words across linebreaks`() {
        val input = "This is a demon- \n stration of hyphen- \nated text."
        val cleaned = TextCleanupUtil.fixHyphenatedLineBreaks(input)
        assertTrue(cleaned.contains("demonstration"))
        assertTrue(cleaned.contains("hyphenated"))
    }

    @Test
    fun `fixHyphenatedLineBreaks merges Devanagari words across linebreaks`() {
        val input = "पाठ्यपुस्तक का उदा-\nहरण देखिए।"
        val cleaned = TextCleanupUtil.fixHyphenatedLineBreaks(input)
        assertTrue(cleaned.contains("उदाहरण"))
    }

    @Test
    fun `removeNoiseCharacters strips OCR specks while preserving Indic dandas`() {
        val input = "पेड़ हमें फल देते हैं । ~ सूर्य प्रकाश देता है ॥"
        val cleaned = TextCleanupUtil.cleanupOcrText(input)
        assertFalse(cleaned.contains("~"))
        assertTrue(cleaned.contains("देते हैं।"))
        assertTrue(cleaned.contains("देता है॥"))
    }

    @Test
    fun `collapseStraySpaces fixes whitespace around punctuation`() {
        val input = "Hello   world  ,  how are you  ? Good  ."
        val cleaned = TextCleanupUtil.cleanupOcrText(input)
        assertEquals("Hello world, how are you? Good.", cleaned)
    }

    @Test
    fun `preserveParagraphBreaks maintains double newlines while joining soft linebreaks`() {
        val input = """
            First paragraph line one
            and line two of first paragraph.
            
            Second paragraph line one
            and line two.
        """.trimIndent()

        val cleaned = TextCleanupUtil.cleanupOcrText(input)
        val paragraphs = cleaned.split("\n\n")
        assertEquals(2, paragraphs.size)
        assertEquals("First paragraph line one and line two of first paragraph.", paragraphs[0])
        assertEquals("Second paragraph line one and line two.", paragraphs[1])
    }

    @Test
    fun `cleanupOcrText handles complex multilingual Hindi and English sample`() {
        val rawHindiSample = """
            विज्ञान पाठ १ ~
            सौरमंडल में आठ ग्रह हैं ।
            सूर्य केंद्र में स्थित
            है ।
            
            The solar sys-
            tem has eight planets.
        """.trimIndent()

        val cleaned = TextCleanupUtil.cleanupOcrText(rawHindiSample)
        assertFalse(cleaned.contains("~"))
        assertTrue(cleaned.contains("सौरमंडल में आठ ग्रह हैं।"))
        assertTrue(cleaned.contains("solar system has eight planets."))
    }
}
