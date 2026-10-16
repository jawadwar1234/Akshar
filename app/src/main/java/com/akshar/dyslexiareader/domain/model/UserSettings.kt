package com.akshar.dyslexiareader.domain.model

import com.akshar.dyslexiareader.util.AppLanguage

data class UserSettings(
    val parentalConsentGiven: Boolean = false,
    val firstRunCompleted: Boolean = false,
    val activeProfileId: String? = null,
    val parentAccountId: String? = null,
    val isOfflineMode: Boolean = true,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val rulerEnabled: Boolean = false,
    val rulerWindowLines: Int = 1, // 1 line or 3 lines focus window
    val bionicEnabled: Boolean = false,
    val syllablesEnabled: Boolean = false,
    val chunkingPaletteIndex: Int = 0, // 0: Blue/Amber, 1: Sage/Plum, 2: Teal/Terracotta
    val readerTheme: String = "cream",
    val fontSize: Float = 22f,
    val lineSpacing: Float = 1.6f,
    val letterSpacing: Float = 0.08f,
    val wordSpacing: Float = 0.1f,
    val marginWidth: Int = 16,
    val fontFamily: String = "lexend",
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val backupPhotosToCloud: Boolean = false,
    val backupPhotosWifiOnly: Boolean = true
)
