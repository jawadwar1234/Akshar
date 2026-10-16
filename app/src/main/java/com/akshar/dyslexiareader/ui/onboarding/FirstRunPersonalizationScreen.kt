package com.akshar.dyslexiareader.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CalmTealContainer
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.DyslexiaFontOption
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.ReaderThemePreset
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import com.akshar.dyslexiareader.ui.theme.TerracottaAccentContainer
import com.akshar.dyslexiareader.ui.theme.WarmForestGreen

@Composable
fun FirstRunPersonalizationScreen(
    onComplete: (theme: String, fontSize: Float, lineSpacing: Float, letterSpacing: Float, fontFamily: String, rulerEnabled: Boolean) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }

    // Personalization state with dyslexic-friendly defaults
    var selectedThemeId by remember { mutableStateOf("cream") }
    var fontSizeSp by remember { mutableFloatStateOf(24f) }
    var lineSpacingMultiplier by remember { mutableFloatStateOf(1.6f) }
    var letterSpacingEm by remember { mutableFloatStateOf(0.08f) }
    var selectedFontId by remember { mutableStateOf("lexend") }
    var rulerEnabled by remember { mutableStateOf(false) }

    val activeThemePreset = ReaderThemePreset.fromId(selectedThemeId)
    val chosenFontOption = DyslexiaFontOption.fromId(selectedFontId)

    fun finishWithCurrentOrDefaults() {
        onComplete(
            selectedThemeId,
            fontSizeSp,
            lineSpacingMultiplier,
            letterSpacingEm,
            selectedFontId,
            rulerEnabled
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.Spacing.lg, vertical = DesignTokens.Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..3).forEach { step ->
                        Box(
                            modifier = Modifier
                                .size(if (step == currentStep) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (step == currentStep) TerracottaAccent else PaperCreamBorder)
                        )
                    }
                }

                // Skip Button
                TextButton(onClick = ::finishWithCurrentOrDefaults) {
                    Text(
                        text = "Skip",
                        color = CharcoalTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        containerColor = PaperCreamBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = DesignTokens.Spacing.lg)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                    } else {
                        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                    }
                },
                label = "TutorialSteps"
            ) { step ->
                when (step) {
                    1 -> TutorialStepOneTheme(
                        selectedThemeId = selectedThemeId,
                        onThemeSelected = { selectedThemeId = it }
                    )
                    2 -> TutorialStepTwoTypography(
                        activeTheme = activeThemePreset,
                        fontFamily = chosenFontOption.fontFamily,
                        selectedFontId = selectedFontId,
                        fontSizeSp = fontSizeSp,
                        lineSpacingMultiplier = lineSpacingMultiplier,
                        letterSpacingEm = letterSpacingEm,
                        onFontSelected = { selectedFontId = it },
                        onFontSizeChange = { fontSizeSp = it },
                        onLineSpacingChange = { lineSpacingMultiplier = it },
                        onLetterSpacingChange = { letterSpacingEm = it }
                    )
                    3 -> TutorialStepThreeTools(
                        rulerEnabled = rulerEnabled,
                        onRulerToggle = { rulerEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

            // Navigation Buttons Bottom Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = DesignTokens.Spacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .height(DesignTokens.buttonHeight)
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous step", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                        Text("Back", color = CharcoalTextPrimary)
                    }
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))
                }

                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            finishWithCurrentOrDefaults()
                        }
                    },
                    modifier = Modifier
                        .height(DesignTokens.buttonHeight)
                        .weight(if (currentStep > 1) 1.4f else 1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentStep < 3) "Next" else "Let's Start Reading!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (currentStep < 3) {
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next step", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialStepOneTheme(
    selectedThemeId: String,
    onThemeSelected: (String) -> Unit
) {
    val presets = listOf(
        Pair(ReaderThemePreset.CREAM, "Warm Cream"),
        Pair(ReaderThemePreset.WARM_WHITE, "Soft White"),
        Pair(ReaderThemePreset.SEPIA, "Sepia Paper"),
        Pair(ReaderThemePreset.CHARCOAL_NIGHT, "Charcoal Night"),
        Pair(ReaderThemePreset.PALE_BLUE, "Pale Blue")
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = TerracottaAccentContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ColorLens, contentDescription = null, tint = TerracottaAccent, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        Text(
            text = "Welcome to Akshar! 📖",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CharcoalTextPrimary,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))

        Text(
            text = "Choose a calming background tint that reduces glare and visual fatigue for your eyes.",
            style = MaterialTheme.typography.bodyMedium,
            color = CharcoalTextSecondary,
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.md)
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

        // Themes list
        presets.forEach { (preset, label) ->
            val isSelected = preset.id == selectedThemeId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onThemeSelected(preset.id) }
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) TerracottaAccent else PaperCreamBorder,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = preset.background)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.Spacing.lg, vertical = DesignTokens.Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = preset.textPrimary
                        )
                        Text(
                            text = "Sample reading text on ${label.lowercase()}",
                            fontSize = 12.sp,
                            color = preset.textSecondary
                        )
                    }

                    if (isSelected) {
                        Surface(shape = CircleShape, color = TerracottaAccent, modifier = Modifier.size(26.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = "Selected theme", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialStepTwoTypography(
    activeTheme: ReaderThemePreset,
    fontFamily: FontFamily,
    selectedFontId: String,
    fontSizeSp: Float,
    lineSpacingMultiplier: Float,
    letterSpacingEm: Float,
    onFontSelected: (String) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onLetterSpacingChange: (Float) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = CalmTealContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.FormatSize, contentDescription = null, tint = CalmTeal, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        Text(
            text = "Dyslexia-Friendly Type",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CharcoalTextPrimary,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))

        Text(
            text = "Adjust font size and generous spacing to prevent letter crowding.",
            style = MaterialTheme.typography.bodyMedium,
            color = CharcoalTextSecondary
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Live Reader Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .border(1.5.dp, PaperCreamBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = activeTheme.background)
        ) {
            Column(modifier = Modifier.padding(DesignTokens.Spacing.lg)) {
                Text(
                    text = "Live Preview:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeTheme.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Reading opens up new worlds of wonder and imagination every single day.",
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * lineSpacingMultiplier).sp,
                    letterSpacing = letterSpacingEm.em,
                    fontFamily = fontFamily,
                    color = activeTheme.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Font Family Selector
        Text(text = "Choose Dyslexia Font:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalTextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("lexend", "Lexend", "Clean"),
                Triple("open_dyslexic", "OpenDyslexic", "Weighted"),
                Triple("noto_sans", "Noto Sans", "Indic")
            ).forEach { item ->
                val isSelected = selectedFontId == item.first
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) TerracottaAccent else PaperCreamSurface,
                    border = BorderStroke(1.dp, if (isSelected) TerracottaAccent else PaperCreamBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onFontSelected(item.first) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.second,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else CharcoalTextPrimary
                        )
                        Text(
                            text = item.third,
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else CharcoalTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Font Size Slider
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Font Size", fontSize = 13.sp, color = CharcoalTextSecondary)
            Text(text = "${fontSizeSp.toInt()} sp", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalTextPrimary)
        }
        Slider(
            value = fontSizeSp,
            onValueChange = onFontSizeChange,
            valueRange = 18f..32f,
            colors = SliderDefaults.colors(thumbColor = TerracottaAccent, activeTrackColor = TerracottaAccent)
        )

        // Line Spacing Slider
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Line Spacing", fontSize = 13.sp, color = CharcoalTextSecondary)
            Text(text = "${String.format(java.util.Locale.ROOT, "%.1f", lineSpacingMultiplier)}x", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CharcoalTextPrimary)
        }
        Slider(
            value = lineSpacingMultiplier,
            onValueChange = onLineSpacingChange,
            valueRange = 1.3f..2.2f,
            colors = SliderDefaults.colors(thumbColor = CalmTeal, activeTrackColor = CalmTeal)
        )
    }
}

@Composable
private fun TutorialStepThreeTools(
    rulerEnabled: Boolean,
    onRulerToggle: (Boolean) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = WarmForestGreen.copy(alpha = 0.15f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = WarmForestGreen, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        Text(
            text = "Gentle Reading Tools",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CharcoalTextPrimary,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))

        Text(
            text = "Built-in tools designed to build reading confidence without stress.",
            style = MaterialTheme.typography.bodyMedium,
            color = CharcoalTextSecondary
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

        // Tool 1: Reading Ruler
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PaperCreamBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ViewHeadline, contentDescription = null, tint = CalmTeal, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))
                    Column {
                        Text("Reading Ruler Overlay", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalTextPrimary)
                        Text("Isolates one focus line at a time to eliminate crowding.", fontSize = 11.sp, color = CharcoalTextSecondary)
                    }
                }

                Switch(
                    checked = rulerEnabled,
                    onCheckedChange = onRulerToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = CalmTeal, checkedTrackColor = CalmTealContainer)
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        // Tool 2: Voice Practice Mode intro
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PaperCreamBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = TerracottaAccent, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))
                Column {
                    Text("Gentle Voice Practice", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalTextPrimary)
                    Text("Read aloud sentence by sentence. No timers, no red marks, zero pressure.", fontSize = 11.sp, color = CharcoalTextSecondary)
                }
            }
        }
    }
}
