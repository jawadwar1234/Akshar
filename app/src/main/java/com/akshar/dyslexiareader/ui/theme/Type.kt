package com.akshar.dyslexiareader.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Typography System for Akshar DyslexiaReader.
 * Strict Dyslexia & Low-Vision Rules:
 * 1. Left-aligned ONLY (Never justified).
 * 2. No italics (italics distort glyph shapes).
 * 3. No ALL-CAPS (distinct ascending/descending letter forms ease word shape recognition).
 * 4. Generous line height (1.5x - 1.6x minimum) to prevent vertical visual crowding.
 * 5. Clear, distinct letterforms (Lexend, OpenDyslexic, Noto Sans).
 */

enum class DyslexiaFontOption(
    val id: String,
    val displayName: String,
    val fontFamily: FontFamily
) {
    LEXEND("lexend", "Lexend (Clear & Clean)", FontFamily.SansSerif),
    OPEN_DYSLEXIC("open_dyslexic", "OpenDyslexic (Weighted Bottoms)", FontFamily.Serif),
    NOTO_SANS("noto_sans", "Noto Sans (Indic Optimized)", FontFamily.Default),
    SYSTEM_MONO("system_mono", "Monospace (Fixed Width)", FontFamily.Monospace);

    companion object {
        fun fromId(id: String): DyslexiaFontOption {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: LEXEND
        }
    }
}

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.5.sp,
        textAlign = TextAlign.Start
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.4.sp,
        textAlign = TextAlign.Start
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.3.sp,
        textAlign = TextAlign.Start
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.25.sp,
        textAlign = TextAlign.Start
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.2.sp,
        textAlign = TextAlign.Start
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        textAlign = TextAlign.Start
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp,
        textAlign = TextAlign.Start
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp,
        textAlign = TextAlign.Start
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        textAlign = TextAlign.Start
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp,
        textAlign = TextAlign.Start
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.15.sp,
        textAlign = TextAlign.Start
    )
)
