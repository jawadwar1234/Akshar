package com.akshar.dyslexiareader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.domain.model.UserSettings
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.DyslexiaFontOption
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.ReaderThemePreset
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextSettingsBottomSheet(
    userSettings: UserSettings,
    onDismiss: () -> Unit,
    onUpdateTheme: (String) -> Unit,
    onUpdateFontSize: (Float) -> Unit,
    onUpdateLineSpacing: (Float) -> Unit,
    onUpdateLetterSpacing: (Float) -> Unit,
    onUpdateFontFamily: (String) -> Unit,
    onToggleBionic: () -> Unit,
    onToggleSyllables: () -> Unit,
    onUpdateTtsSpeed: (Float) -> Unit,
    onUpdateTtsPitch: (Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PaperCreamBackground,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing.lg)
                .padding(bottom = DesignTokens.Spacing.xxl)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.reader_btn_appearance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalTextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_close),
                        tint = CharcoalTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // 1. Theme Presets (Cream, Warm White, Sepia, Charcoal Night, Pale Blue)
            Text(
                text = stringResource(R.string.reader_theme_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalTextPrimary
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReaderThemePreset.entries.forEach { preset ->
                    val isSelected = userSettings.readerTheme.equals(preset.id, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(preset.background, shape = CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) TerracottaAccent else PaperCreamBorder,
                                shape = CircleShape
                            )
                            .clickable { onUpdateTheme(preset.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aa",
                            color = preset.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

            // 2. Font Size Slider (16sp - 36sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.reader_font_size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalTextPrimary
                )
                Text(
                    text = "${userSettings.fontSize.toInt()} sp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalTextSecondary
                )
            }
            Slider(
                value = userSettings.fontSize,
                onValueChange = onUpdateFontSize,
                valueRange = 16f..36f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = TerracottaAccent,
                    activeTrackColor = TerracottaAccent
                )
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // 3. Line Spacing Slider (1.4x - 2.2x)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.reader_line_spacing),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalTextPrimary
                )
                Text(
                    text = String.format("%.1fx", userSettings.lineSpacing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalTextSecondary
                )
            }
            Slider(
                value = userSettings.lineSpacing,
                onValueChange = onUpdateLineSpacing,
                valueRange = 1.4f..2.2f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = CalmTeal,
                    activeTrackColor = CalmTeal
                )
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // 4. Letter Spacing Slider (0.04 - 0.20)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.reader_letter_spacing),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalTextPrimary
                )
                Text(
                    text = String.format("%.2f", userSettings.letterSpacing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalTextSecondary
                )
            }
            Slider(
                value = userSettings.letterSpacing,
                onValueChange = onUpdateLetterSpacing,
                valueRange = 0.04f..0.20f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = TerracottaAccent,
                    activeTrackColor = TerracottaAccent
                )
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // 5. Font Family Choice
            Text(
                text = stringResource(R.string.reader_font_family),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalTextPrimary
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
            DyslexiaFontOption.entries.forEach { fontOption ->
                val isSelected = userSettings.fontFamily.equals(fontOption.id, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { onUpdateFontFamily(fontOption.id) },
                    label = { Text(fontOption.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TerracottaAccent.copy(alpha = 0.15f),
                        selectedLabelColor = TerracottaAccent
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // 6. Dyslexia Reading Tools Toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleBionic() }
                    .padding(vertical = DesignTokens.Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reader_bionic_reading),
                        style = MaterialTheme.typography.titleMedium,
                        color = CharcoalTextPrimary
                    )
                    Text(
                        text = "Highlights initial letters to guide eye fixation",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary
                    )
                }
                Switch(
                    checked = userSettings.bionicEnabled,
                    onCheckedChange = { onToggleBionic() },
                    colors = SwitchDefaults.colors(checkedThumbColor = TerracottaAccent)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleSyllables() }
                    .padding(vertical = DesignTokens.Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reader_syllables_toggle),
                        style = MaterialTheme.typography.titleMedium,
                        color = CharcoalTextPrimary
                    )
                    Text(
                        text = "Separates words into readable sound units",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary
                    )
                }
                Switch(
                    checked = userSettings.syllablesEnabled,
                    onCheckedChange = { onToggleSyllables() },
                    colors = SwitchDefaults.colors(checkedThumbColor = CalmTeal)
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // 7. TTS Speed & Pitch Controls
            Text(
                text = stringResource(R.string.reader_tts_speed),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalTextPrimary
            )
            Slider(
                value = userSettings.ttsSpeed,
                onValueChange = onUpdateTtsSpeed,
                valueRange = 0.5f..1.5f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = CalmTeal,
                    activeTrackColor = CalmTeal
                )
            )
        }
    }
}
