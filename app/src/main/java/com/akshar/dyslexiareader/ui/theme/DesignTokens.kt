package com.akshar.dyslexiareader.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design Tokens for Akshar DyslexiaReader.
 * Built strictly according to low-cognitive-load, high-readability dyslexia guidelines.
 * - 8dp spacing grid
 * - Minimum 56dp touch targets
 * - Moderate 8-12dp radii
 * - 1dp flat surface borders (no heavy drop shadows)
 */
object DesignTokens {
    // Spacing Grid (8dp base)
    object Spacing {
        val xxs: Dp = 2.dp
        val xs: Dp = 4.dp
        val sm: Dp = 8.dp
        val md: Dp = 16.dp
        val lg: Dp = 24.dp
        val xl: Dp = 32.dp
        val xxl: Dp = 48.dp
        val xxxl: Dp = 64.dp
    }

    // Accessibility & Touch Target Minimums
    val minTouchTarget: Dp = 56.dp
    val buttonHeight: Dp = 56.dp
    val iconSizeSmall: Dp = 24.dp
    val iconSizeMedium: Dp = 32.dp
    val iconSizeLarge: Dp = 48.dp

    // Corner Radii (Moderate, never overly bubbly)
    object Radii {
        val none: Dp = 0.dp
        val sm: Dp = 6.dp
        val md: Dp = 10.dp
        val lg: Dp = 14.dp
        val pill: Dp = 100.dp
    }

    // Border Widths (Subtle, crisp 1dp outlines)
    object ElevationAndBorders {
        val borderThin: Dp = 1.dp
        val borderThick: Dp = 2.dp
        val borderFocus: Dp = 3.dp
        val cardElevation: Dp = 0.dp // Flat surfaces per design direction
    }

    // Typography Scale (Spacings, Line Heights, Letter Spacings)
    object TypographyTokens {
        val lineSpacingMultiplierDefault: Float = 1.6f
        val lineSpacingMultiplierLoose: Float = 1.9f
        val lineSpacingMultiplierExtraLoose: Float = 2.2f

        val letterSpacingDefault: TextUnit = 0.06.sp
        val letterSpacingWide: TextUnit = 0.12.sp
        val letterSpacingExtraWide: TextUnit = 0.18.sp

        val minReaderFontSize: TextUnit = 16.sp
        val defaultReaderFontSize: TextUnit = 22.sp
        val maxReaderFontSize: TextUnit = 38.sp
    }
}
