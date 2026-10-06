package com.akshar.dyslexiareader.domain.util

import com.akshar.dyslexiareader.domain.model.SupportedScript
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class UnicodeIndicTextUtilTest {

    @Test
    fun extractGraphemeClusters_preservesDevanagariMatrasAndConjuncts() {
        val hindiWord = "नमस्ते" // न + म + स्ते
        val clusters = UnicodeIndicTextUtil.extractGraphemeClusters(hindiWord, Locale("hi", "IN"))
        
        assertEquals(3, clusters.size)
        assertEquals("न", clusters[0])
        assertEquals("म", clusters[1])
        assertEquals("स्ते", clusters[2]) // Conjunct + matra never broken!
    }

    @Test
    fun extractGraphemeClusters_preservesMarathiWords() {
        val marathiWord = "ज्ञान" // ज्ञा + न
        val clusters = UnicodeIndicTextUtil.extractGraphemeClusters(marathiWord, Locale("mr", "IN"))
        
        assertEquals(2, clusters.size)
        assertEquals("ज्ञा", clusters[0]) // Gya + aa matra
        assertEquals("न", clusters[1])
    }

    @Test
    fun extractGraphemeClusters_preservesGujaratiConjuncts() {
        val gujaratiWord = "નમસ્તે"
        val clusters = UnicodeIndicTextUtil.extractGraphemeClusters(gujaratiWord, Locale("gu", "IN"))
        
        assertEquals(3, clusters.size)
        assertEquals("ન", clusters[0])
        assertEquals("મ", clusters[1])
        assertEquals("સ્તે", clusters[2])
    }

    @Test
    fun extractGraphemeClusters_preservesBengaliConjuncts() {
        val bengaliWord = "নমস্কার" // ন + ম + স্কা + র
        val clusters = UnicodeIndicTextUtil.extractGraphemeClusters(bengaliWord, Locale("bn", "IN"))
        
        assertEquals(4, clusters.size)
        assertEquals("ন", clusters[0])
        assertEquals("ম", clusters[1])
        assertEquals("স্কা", clusters[2])
        assertEquals("র", clusters[3])
    }

    @Test
    fun extractSentenceSpans_splitsSentencesCorrectly() {
        val text = "यह एक किताब है। इसमें अच्छी कहानियां हैं! क्या आप पढ़ेंगे?"
        val spans = UnicodeIndicTextUtil.extractSentenceSpans(text, Locale("hi", "IN"))
        
        assertTrue(spans.size >= 3)
        assertEquals("यह एक किताब है।", spans[0].sentence)
    }

    @Test
    fun extractWordSpans_filtersPunctuationAndCalculatesOffsets() {
        val text = "अक्षर, पुस्तक और विद्या।"
        val wordSpans = UnicodeIndicTextUtil.extractWordSpans(text, Locale("hi", "IN"))
        
        assertEquals(4, wordSpans.size)
        assertEquals("अक्षर", wordSpans[0].cleanWord)
        assertEquals("पुस्तक", wordSpans[1].cleanWord)
        assertEquals("और", wordSpans[2].cleanWord)
        assertEquals("विद्या", wordSpans[3].cleanWord)
    }

    @Test
    fun parseReaderText_detectsScriptAndBuildsFullModel() {
        val raw = "  सूर्य पूर्व दिशेला उगवतो.   \n\n  तो आपल्याला प्रकाश देतो. "
        val result = UnicodeIndicTextUtil.parseReaderText(raw)
        
        assertEquals(SupportedScript.DEVANAGARI, result.script)
        assertEquals(2, result.sentences.size)
        assertFalse(result.fullText.startsWith(" "))
    }

    @Test
    fun splitForBionicFocus_splitsEvenly() {
        val word = "dyslexia"
        val (prefix, suffix) = PhonicsSyllableUtil.splitForBionicFocus(word, Locale.ENGLISH)
        assertEquals("dys", prefix)
        assertEquals("lexia", suffix)
    }
}
