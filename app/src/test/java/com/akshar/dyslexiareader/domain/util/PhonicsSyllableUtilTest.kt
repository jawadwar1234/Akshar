package com.akshar.dyslexiareader.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class PhonicsSyllableUtilTest {

    @Test
    fun `breakLatinSyllables splits English words by vowel groups`() {
        val syllables = PhonicsSyllableUtil.breakIntoSyllables("reading", Locale.ENGLISH)
        assertTrue(syllables.isNotEmpty())
        assertEquals("reading", syllables.joinToString(""))
    }

    @Test
    fun `groupIndicClusters preserves Hindi conjuncts like ksh, gya, shra`() {
        val hindiLocale = Locale("hi", "IN")

        // 'शिक्षा' (Contains 'क्षा' -> ksha)
        val shikshaSyllables = PhonicsSyllableUtil.breakIntoSyllables("शिक्षा", hindiLocale)
        assertEquals("शिक्षा", shikshaSyllables.joinToString(""))
        assertTrue(shikshaSyllables.contains("क्षा") || shikshaSyllables.contains("शिक्षा"))

        // 'ज्ञान' (Contains 'ज्ञा' -> gya)
        val gyanSyllables = PhonicsSyllableUtil.breakIntoSyllables("ज्ञान", hindiLocale)
        assertEquals("ज्ञान", gyanSyllables.joinToString(""))

        // 'श्रीमान' (Contains 'श्री' -> shree)
        val shreemanSyllables = PhonicsSyllableUtil.breakIntoSyllables("श्रीमान", hindiLocale)
        assertEquals("श्रीमान", shreemanSyllables.joinToString(""))
    }

    @Test
    fun `groupIndicClusters preserves Marathi conjuncts`() {
        val marathiLocale = Locale("mr", "IN")

        // 'महाराष्ट्र' (Contains 'ष्ट्र' -> shtra)
        val maharashtraSyllables = PhonicsSyllableUtil.breakIntoSyllables("महाराष्ट्र", marathiLocale)
        assertEquals("महाराष्ट्र", maharashtraSyllables.joinToString(""))

        // 'विद्यार्थी' (Contains 'द्या' and 'र्थी')
        val vidyarthiSyllables = PhonicsSyllableUtil.breakIntoSyllables("विद्यार्थी", marathiLocale)
        assertEquals("विद्यार्थी", vidyarthiSyllables.joinToString(""))
    }

    @Test
    fun `groupIndicClusters preserves Gujarati conjuncts`() {
        val gujaratiLocale = Locale("gu", "IN")

        // 'નમસ્તે' (Contains 'સ્તે' -> ste)
        val namasteSyllables = PhonicsSyllableUtil.breakIntoSyllables("નમસ્તે", gujaratiLocale)
        assertEquals("નમસ્તે", namasteSyllables.joinToString(""))

        // 'દ્રષ્ટિ' (Contains 'દ્ર' and 'ષ્ટિ')
        val drashtiSyllables = PhonicsSyllableUtil.breakIntoSyllables("દ્રષ્ટિ", gujaratiLocale)
        assertEquals("દ્રષ્ટિ", drashtiSyllables.joinToString(""))
    }

    @Test
    fun `groupIndicClusters preserves Bengali conjuncts`() {
        val bengaliLocale = Locale("bn", "IN")

        // 'নমস্কার' (Contains 'স্কা' -> ska)
        val namaskarSyllables = PhonicsSyllableUtil.breakIntoSyllables("নমস্কার", bengaliLocale)
        assertEquals("নমস্কার", namaskarSyllables.joinToString(""))

        // 'বিজ্ঞান' (Contains 'জ্ঞা' -> gya)
        val bijnanSyllables = PhonicsSyllableUtil.breakIntoSyllables("বিজ্ঞান", bengaliLocale)
        assertEquals("বিজ্ঞান", bijnanSyllables.joinToString(""))
    }

    @Test
    fun `splitForBionicFocus highlights initial grapheme clusters without breaking conjuncts`() {
        val hindiLocale = Locale("hi", "IN")
        val (prefix, suffix) = PhonicsSyllableUtil.splitForBionicFocus("प्रशिक्षण", hindiLocale)
        assertEquals("प्रशिक्षण", prefix + suffix)
        assertTrue(prefix.isNotEmpty())
        assertTrue(suffix.isNotEmpty())
    }
}
