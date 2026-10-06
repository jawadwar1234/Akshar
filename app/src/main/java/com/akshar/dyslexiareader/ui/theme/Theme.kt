package com.akshar.dyslexiareader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalReaderTheme = staticCompositionLocalOf { ReaderThemePreset.CREAM }

private val LightColorScheme = lightColorScheme(
    primary = TerracottaAccent,
    onPrimary = TerracottaOnAccent,
    primaryContainer = TerracottaAccentContainer,
    onPrimaryContainer = TerracottaAccentHover,
    secondary = CalmTeal,
    onSecondary = Color.White,
    secondaryContainer = CalmTealContainer,
    onSecondaryContainer = CalmTealOnContainer,
    background = PaperCreamBackground,
    onBackground = CharcoalTextPrimary,
    surface = PaperCreamSurface,
    onSurface = CharcoalTextPrimary,
    surfaceVariant = PaperCreamSurfaceVariant,
    onSurfaceVariant = CharcoalTextSecondary,
    outline = PaperCreamBorder,
    error = GentleErrorMuted,
    errorContainer = GentleErrorContainer,
    onError = Color.White,
    onErrorContainer = GentleErrorMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE57E56),
    onPrimary = Color(0xFF381405),
    primaryContainer = Color(0xFF5E2712),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFF5CB3B0),
    onSecondary = Color(0xFF043735),
    secondaryContainer = Color(0xFF1E4E4C),
    onSecondaryContainer = Color(0xFFBCEAE7),
    background = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE4E0D7),
    surface = Color(0xFF2A2A2A),
    onSurface = Color(0xFFE4E0D7),
    surfaceVariant = Color(0xFF353535),
    onSurfaceVariant = Color(0xFFA8A399),
    outline = Color(0xFF454545),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun DyslexiaReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    readerTheme: ReaderThemePreset = ReaderThemePreset.CREAM,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalReaderTheme provides readerTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
