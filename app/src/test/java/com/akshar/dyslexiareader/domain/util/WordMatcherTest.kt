package com.akshar.dyslexiareader.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordMatcherTest {

    @Test
    fun isFuzzyMatch_matchesExactWords() {
        assertTrue(WordMatcher.isFuzzyMatch("नमस्ते", "नमस्ते"))
        assertTrue(WordMatcher.isFuzzyMatch("book", "book"))
        assertTrue(WordMatcher.isFuzzyMatch("પુસ્તક", "પુસ્તક"))
        assertTrue(WordMatcher.isFuzzyMatch("বই", "বই"))
    }

    @Test
    fun isFuzzyMatch_ignoresPunctuationAndCase() {
        assertTrue(WordMatcher.isFuzzyMatch("Reading!", "reading"))
        assertTrue(WordMatcher.isFuzzyMatch("अक्षर।", "अक्षर"))
        assertTrue(WordMatcher.isFuzzyMatch("નમસ્તે,", "નમસ્તે"))
    }

    @Test
    fun isFuzzyMatch_forgivesMinorPhoneticErrors() {
        // Minor typo/accent variation in speech recognition
        assertTrue(WordMatcher.isFuzzyMatch("readin", "reading"))
        assertTrue(WordMatcher.isFuzzyMatch("akshara", "akshar"))
    }

    @Test
    fun isFuzzyMatch_rejectsCompletelyDifferentWords() {
        assertFalse(WordMatcher.isFuzzyMatch("elephant", "apple"))
        assertFalse(WordMatcher.isFuzzyMatch("किताब", "सूरज"))
    }

    @Test
    fun findMatchedWordsInSentence_identifiesSpokenWords() {
        val spoken = "सूर्य पूर्व दिशेला उगवतो"
        val target = listOf("सूर्य", "पूर्व", "दिशेला", "उगवतो")
        val matches = WordMatcher.findMatchedWordsInSentence(spoken, target)

        assertEquals(4, matches.size)
        assertTrue(matches.contains("सूर्य"))
    }
}
