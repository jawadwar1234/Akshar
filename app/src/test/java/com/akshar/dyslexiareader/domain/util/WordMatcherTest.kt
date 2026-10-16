package com.akshar.dyslexiareader.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordMatcherTest {

    // ==========================================
    // 1. EXACT MATCH & CASE / PUNCTUATION
    // ==========================================

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
        assertTrue(WordMatcher.isFuzzyMatch("«नमस्ते»", "नमस्ते"))
        assertTrue(WordMatcher.isFuzzyMatch("  Butterfly!  ", "butterfly"))
        assertTrue(WordMatcher.isFuzzyMatch("What?", "what"))
        assertTrue(WordMatcher.isFuzzyMatch("भारत॥", "भारत"))
    }

    // ==========================================
    // 2. ENGLISH PHONETIC & TYPO FORGIVENESS
    // ==========================================

    @Test
    fun isFuzzyMatch_englishPhoneticAndDigraphSubstitutions() {
        // 'ph' vs 'f'
        assertTrue(WordMatcher.isFuzzyMatch("elefant", "elephant"))
        assertTrue(WordMatcher.isFuzzyMatch("fone", "phone"))
        assertTrue(WordMatcher.isFuzzyMatch("fotos", "photos"))

        // 'kn' vs 'n' and 'wr' vs 'r'
        assertTrue(WordMatcher.isFuzzyMatch("nite", "knight"))
        assertTrue(WordMatcher.isFuzzyMatch("rite", "write"))
        assertTrue(WordMatcher.isFuzzyMatch("now", "know"))

        // 'qu' vs 'kw' / 'k'
        assertTrue(WordMatcher.isFuzzyMatch("kwik", "quick"))

        // Trailing '-ing' vs '-in'
        assertTrue(WordMatcher.isFuzzyMatch("readin", "reading"))
        assertTrue(WordMatcher.isFuzzyMatch("runnin", "running"))
        assertTrue(WordMatcher.isFuzzyMatch("jumpin", "jumping"))

        // Double consonants collapse
        assertTrue(WordMatcher.isFuzzyMatch("buterfly", "butterfly"))
        assertTrue(WordMatcher.isFuzzyMatch("litle", "little"))
        assertTrue(WordMatcher.isFuzzyMatch("aple", "apple"))
        assertTrue(WordMatcher.isFuzzyMatch("hapen", "happen"))

        // Vowel and speech recognition variations
        assertTrue(WordMatcher.isFuzzyMatch("frend", "friend"))
        assertTrue(WordMatcher.isFuzzyMatch("scholl", "school"))
        assertTrue(WordMatcher.isFuzzyMatch("skool", "school"))
    }

    // ==========================================
    // 3. HINDI / DEVANAGARI PHONETIC & CONJUNCT FORGIVENESS
    // ==========================================

    @Test
    fun isFuzzyMatch_hindiNuqtaNormalization() {
        // Nuqta vs Non-nuqta characters
        assertTrue(WordMatcher.isFuzzyMatch("ज़िंदगी", "जिंदगी"))
        assertTrue(WordMatcher.isFuzzyMatch("साफ़", "साफ"))
        assertTrue(WordMatcher.isFuzzyMatch("पेड़", "पेड"))
        assertTrue(WordMatcher.isFuzzyMatch("ख़ुशी", "खुशी"))
        assertTrue(WordMatcher.isFuzzyMatch("ग़रीब", "गरीब"))
        assertTrue(WordMatcher.isFuzzyMatch("क़लम", "कलम"))
    }

    @Test
    fun isFuzzyMatch_hindiAnusvaraAndNasalEquivalence() {
        // Anusvara vs full/half nasal conjuncts
        assertTrue(WordMatcher.isFuzzyMatch("हिंदी", "हिन्दी"))
        assertTrue(WordMatcher.isFuzzyMatch("हँसना", "हंसना"))
        assertTrue(WordMatcher.isFuzzyMatch("पंत", "पन्त"))
        assertTrue(WordMatcher.isFuzzyMatch("संबंध", "सम्बन्ध"))
        assertTrue(WordMatcher.isFuzzyMatch("मंदिर", "मन्दिर"))
    }

    @Test
    fun isFuzzyMatch_hindiVowelMatraTolerance() {
        // Short vs long matras commonly mixed in speech/OCR
        assertTrue(WordMatcher.isFuzzyMatch("कीताब", "किताब"))
        assertTrue(WordMatcher.isFuzzyMatch("किताब", "कीताब"))
        assertTrue(WordMatcher.isFuzzyMatch("सुरज", "सूरज"))
        assertTrue(WordMatcher.isFuzzyMatch("सूरज", "सुरज"))
        assertTrue(WordMatcher.isFuzzyMatch("दीवाली", "दिवाली"))
        assertTrue(WordMatcher.isFuzzyMatch("पनि", "पानी"))
    }

    @Test
    fun isFuzzyMatch_hindiConjunctAndSchwaTolerance() {
        // Speech recognizer transcription slight conjunct deviations
        assertTrue(WordMatcher.isFuzzyMatch("विद्यालय", "विद्यालय"))
        assertTrue(WordMatcher.isFuzzyMatch("विदयालय", "विद्यालय"))
        assertTrue(WordMatcher.isFuzzyMatch("पुसतक", "पुस्तक"))
        assertTrue(WordMatcher.isFuzzyMatch("पुस्तक", "पुसतक"))
        assertTrue(WordMatcher.isFuzzyMatch("परकाश", "प्रकाश"))
        assertTrue(WordMatcher.isFuzzyMatch("प्रकाश", "परकाश"))
        assertTrue(WordMatcher.isFuzzyMatch("ज्ञान", "ग्यान"))
        assertTrue(WordMatcher.isFuzzyMatch("ग्यान", "ज्ञान"))
        assertTrue(WordMatcher.isFuzzyMatch("ऋषि", "रिशि"))
        assertTrue(WordMatcher.isFuzzyMatch("अक्षर", "अक्षर"))
    }

    // ==========================================
    // 4. FALSE POSITIVE REJECTION
    // ==========================================

    @Test
    fun isFuzzyMatch_rejectsCompletelyDifferentWords() {
        // English words
        assertFalse(WordMatcher.isFuzzyMatch("elephant", "apple"))
        assertFalse(WordMatcher.isFuzzyMatch("school", "garden"))
        assertFalse(WordMatcher.isFuzzyMatch("water", "fire"))
        assertFalse(WordMatcher.isFuzzyMatch("house", "window"))

        // Hindi words
        assertFalse(WordMatcher.isFuzzyMatch("किताब", "सूरज"))
        assertFalse(WordMatcher.isFuzzyMatch("पानी", "आग"))
        assertFalse(WordMatcher.isFuzzyMatch("पेड़", "गाड़ी"))
        assertFalse(WordMatcher.isFuzzyMatch("घर", "सड़क"))
    }

    // ==========================================
    // 5. SENTENCE-LEVEL MATCHING (ENGLISH & HINDI)
    // ==========================================

    @Test
    fun findMatchedWordsInSentence_identifiesSpokenHindiSentence() {
        val spoken = "सूर्य पूर्व दिशेला उगवतो"
        val target = listOf("सूर्य", "पूर्व", "दिशेला", "उगवतो")
        val matches = WordMatcher.findMatchedWordsInSentence(spoken, target)

        assertEquals(4, matches.size)
        assertTrue(matches.contains("सूर्य"))
        assertTrue(matches.contains("पूर्व"))
        assertTrue(matches.contains("दिशेला"))
        assertTrue(matches.contains("उगवतो"))
    }

    @Test
    fun findMatchedWordsInSentence_identifiesHindiWithPhoneticVariations() {
        // Child said 'सुरज' for 'सूरज', 'पुसतक' for 'पुस्तक', 'पढता' for 'पढ़ता'
        val spoken = "सुरज पुसतक पढता है"
        val target = listOf("सूरज", "पुस्तक", "पढ़ता", "है")
        val matches = WordMatcher.findMatchedWordsInSentence(spoken, target)

        assertEquals(4, matches.size)
        assertTrue(matches.contains("सूरज"))
        assertTrue(matches.contains("पुस्तक"))
        assertTrue(matches.contains("पढ़ता"))
        assertTrue(matches.contains("है"))
    }

    @Test
    fun findMatchedWordsInSentence_partialReadingMatchEnglish() {
        val spoken = "The kwik brown fox"
        val target = listOf("The", "quick", "brown", "fox", "jumps", "over")
        val matches = WordMatcher.findMatchedWordsInSentence(spoken, target)

        assertEquals(4, matches.size)
        assertTrue(matches.contains("The"))
        assertTrue(matches.contains("quick"))
        assertTrue(matches.contains("brown"))
        assertTrue(matches.contains("fox"))
        assertFalse(matches.contains("jumps"))
    }
}

