package com.akshar.dyslexiareader.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color Palette for Akshar DyslexiaReader.
 * Warm, paper-like background (#FAF4E6), charcoal text (#2B2B2B, never pure black on pure white),
 * Terracotta accent (#C85A32), and Calm Teal (#2D7A78).
 */

// Core App Colors
val PaperCreamBackground = Color(0xFFFAF4E6)
val PaperCreamSurface = Color(0xFFFDFBF7)
val PaperCreamSurfaceVariant = Color(0xFFF3ECE0)
val PaperCreamBorder = Color(0xFFE5DDD0)

val CharcoalTextPrimary = Color(0xFF2B2B2B)
val CharcoalTextSecondary = Color(0xFF5C5750)
val CharcoalTextTertiary = Color(0xFF7A746C)

val TerracottaAccent = Color(0xFFC85A32)
val TerracottaAccentHover = Color(0xFFB34A24)
val TerracottaAccentContainer = Color(0xFFFDECE6)
val TerracottaOnAccent = Color(0xFFFFFFFF)

val CalmTeal = Color(0xFF2D7A78)
val CalmTealContainer = Color(0xFFE2F0EF)
val CalmTealOnContainer = Color(0xFF144544)

val WarmForestGreen = Color(0xFF387B4C)
val WarmForestGreenContainer = Color(0xFFE4F2E8)

val WarmWarningAmber = Color(0xFFB57012)
val WarmWarningAmberContainer = Color(0xFFFDF4E5)

val GentleErrorMuted = Color(0xFFA63D36)
val GentleErrorContainer = Color(0xFFFBECEB)

// 5 Dyslexia Reader Themes
enum class ReaderThemePreset(
    val id: String,
    val displayNameResId: Int,
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val highlightWordBg: Color,
    val highlightSentenceBg: Color,
    val rulerColor: Color,
    val borderColor: Color
) {
    CREAM(
        id = "cream",
        displayNameResId = 0, // Assigned via strings
        background = Color(0xFFFAF4E6),
        surface = Color(0xFFF5EFE0),
        textPrimary = Color(0xFF2B2B2B),
        textSecondary = Color(0xFF5C5750),
        highlightWordBg = Color(0xFFFFDF7A),
        highlightSentenceBg = Color(0xFFFFF3C9),
        rulerColor = Color(0x33C85A32),
        borderColor = Color(0xFFE5DDD0)
    ),
    WARM_WHITE(
        id = "warm_white",
        displayNameResId = 0,
        background = Color(0xFFFCFCF9),
        surface = Color(0xFFF3F3EE),
        textPrimary = Color(0xFF262626),
        textSecondary = Color(0xFF525252),
        highlightWordBg = Color(0xFFFFE58F),
        highlightSentenceBg = Color(0xFFFFF8DB),
        rulerColor = Color(0x282D7A78),
        borderColor = Color(0xFFE0E0DB)
    ),
    SEPIA(
        id = "sepia",
        displayNameResId = 0,
        background = Color(0xFFF4ECD8),
        surface = Color(0xFFEBE0C7),
        textPrimary = Color(0xFF382E24),
        textSecondary = Color(0xFF635647),
        highlightWordBg = Color(0xFFE8D099),
        highlightSentenceBg = Color(0xFFF3E4C0),
        rulerColor = Color(0x33382E24),
        borderColor = Color(0xFFDECDB0)
    ),
    CHARCOAL_NIGHT(
        id = "charcoal_night",
        displayNameResId = 0,
        background = Color(0xFF1E1E1E),
        surface = Color(0xFF2A2A2A),
        textPrimary = Color(0xFFE4E0D7),
        textSecondary = Color(0xFFA8A399),
        highlightWordBg = Color(0xFF705315),
        highlightSentenceBg = Color(0xFF423518),
        rulerColor = Color(0x40E4E0D7),
        borderColor = Color(0xFF3D3D3D)
    ),
    PALE_BLUE(
        id = "pale_blue",
        displayNameResId = 0,
        background = Color(0xFFEAF2F8),
        surface = Color(0xFFDDEAF3),
        textPrimary = Color(0xFF1F2D3A),
        textSecondary = Color(0xFF4C5D6C),
        highlightWordBg = Color(0xFFBCE0FD),
        highlightSentenceBg = Color(0xFFDCEEFC),
        rulerColor = Color(0x332D7A78),
        borderColor = Color(0xFFC7D9E6)
    );

    companion object {
        fun fromId(id: String): ReaderThemePreset {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: CREAM
        }
    }
}
