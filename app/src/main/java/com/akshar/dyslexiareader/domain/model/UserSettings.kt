package com.akshar.dyslexiareader.domain.model

import com.akshar.dyslexiareader.util.AppLanguage

data class UserSettings(
    val parentalConsentGiven: Boolean = false,
    val activeProfileId: String? = null,
    val parentAccountId: String? = null,
    val isOfflineMode: Boolean = true,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val rulerEnabled: Boolean = false,
    val bionicEnabled: Boolean = false,
    val syllablesEnabled: Boolean = false,
    val readerTheme: String = "cream",
    val fontSize: Float = 22f,
    val lineSpacing: Float = 1.6f,
    val letterSpacing: Float = 0.08f,
    val fontFamily: String = "lexend",
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f
)
