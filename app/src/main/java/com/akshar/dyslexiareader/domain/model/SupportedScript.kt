package com.akshar.dyslexiareader.domain.model

import java.util.Locale

enum class SupportedScript(
    val id: String,
    val scriptName: String,
    val primaryLocale: Locale,
    val isTesseractBeta: Boolean = false
) {
    LATIN("latin", "English / Latin", Locale.ENGLISH, false),
    DEVANAGARI("devanagari", "Devanagari (Hindi / Marathi)", Locale("hi", "IN"), false),
    GUJARATI("gujarati", "Gujarati (ગુજરાતી)", Locale("gu", "IN"), true),
    BENGALI("bengali", "Bengali (বাংলা)", Locale("bn", "IN"), true);

    companion object {
        fun fromId(id: String?): SupportedScript {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: LATIN
        }
    }
}
