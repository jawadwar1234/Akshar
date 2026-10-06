package com.akshar.dyslexiareader.domain.util

import com.akshar.dyslexiareader.domain.model.SupportedScript
import org.junit.Assert.assertEquals
import org.junit.Test

class ScriptDetectorTest {

    @Test
    fun detectScript_identifiesDevanagari() {
        val hindi = "भारत हमारा देश है और हम सब भारतीय हैं।"
        assertEquals(SupportedScript.DEVANAGARI, ScriptDetector.detectScript(hindi))

        val marathi = "पुस्तके ही आपली खरी मित्र असतात."
        assertEquals(SupportedScript.DEVANAGARI, ScriptDetector.detectScript(marathi))
    }

    @Test
    fun detectScript_identifiesGujarati() {
        val gujarati = "ગુજરાત ભારતનું એક સુંદર રાજ્ય છે."
        assertEquals(SupportedScript.GUJARATI, ScriptDetector.detectScript(gujarati))
    }

    @Test
    fun detectScript_identifiesBengali() {
        val bengali = "আমাদের দেশ ভারতবর্ষ, এখানে বহু ভাষা বলা হয়।"
        assertEquals(SupportedScript.BENGALI, ScriptDetector.detectScript(bengali))
    }

    @Test
    fun detectScript_identifiesLatin() {
        val english = "The quick brown fox jumps over the lazy dog."
        assertEquals(SupportedScript.LATIN, ScriptDetector.detectScript(english))
    }

    @Test
    fun detectScript_handlesEmptyOrSymbols() {
        assertEquals(SupportedScript.LATIN, ScriptDetector.detectScript(""))
        assertEquals(SupportedScript.LATIN, ScriptDetector.detectScript("123 456 !!!"))
    }
}
