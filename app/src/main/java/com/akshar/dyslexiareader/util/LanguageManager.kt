package com.akshar.dyslexiareader.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    MARATHI("mr", "Marathi", "मराठी"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી"),
    BENGALI("bn", "Bengali", "বাংলা");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

object LanguageManager {
    /**
     * Applies the chosen app language dynamically using AndroidX AppCompat per-app language APIs.
     */
    fun setAppLanguage(language: AppLanguage) {
        val appLocale = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Retrieves the currently active application locale.
     */
    fun getCurrentLanguage(context: Context): AppLanguage {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (!currentLocales.isEmpty) {
            val tag = currentLocales[0]?.language
            return AppLanguage.fromCode(tag)
        }
        val defaultLocale = Locale.getDefault().language
        return AppLanguage.fromCode(defaultLocale)
    }
}
