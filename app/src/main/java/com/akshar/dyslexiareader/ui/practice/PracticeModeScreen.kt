package com.akshar.dyslexiareader.ui.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.model.WordSpan
import com.akshar.dyslexiareader.domain.util.PhonicsSyllableUtil
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CalmTealContainer
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import com.akshar.dyslexiareader.ui.theme.TerracottaAccentContainer
import com.akshar.dyslexiareader.ui.theme.WarmForestGreen
import com.akshar.dyslexiareader.ui.theme.WarmForestGreenContainer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PracticeModeScreen(
    documentId: String,
    targetWord: String? = null,
    viewModel: PracticeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToWordMastery: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) viewModel.startListening()
    }

    LaunchedEffect(documentId) {
        viewModel.loadPracticeData(documentId, targetWord)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gentle Reading Practice",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = CharcoalTextPrimary
                        )
                    }
                },
                actions = {
                    // Words Mastered Badge
                    OutlinedButton(
                        onClick = onNavigateToWordMastery,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(end = DesignTokens.Spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = WarmForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                        Text(
                            text = "${uiState.masteredWordsCount} Mastered",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarmForestGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperCreamBackground)
            )
        },
        containerColor = PaperCreamBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = DesignTokens.Spacing.lg)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

            // ==========================================
            // 1. CALM WEEKLY STREAK (Guilt-Free Positive Affirmation)
            // ==========================================
            Card(
                onClick = onNavigateToWordMastery,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(DesignTokens.ElevationAndBorders.borderThin, CalmTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CalmTeal.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(DesignTokens.Spacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Spa, contentDescription = null, tint = CalmTeal, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                            Text(
                                text = "🌱 ${uiState.weeklyDaysRead} reading days this week",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CalmTeal
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "View Mastered Bank",
                            tint = WarmForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

                    // 7-Day Calm Weekly Dots (Mon - Sun)
                    val daysLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        daysLabels.forEachIndexed { index, label ->
                            val isReadDay = uiState.weeklyDaysHistory.getOrElse(index) { false }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isReadDay) CalmTeal else CharcoalTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            color = if (isReadDay) WarmForestGreenContainer else PaperCreamSurface,
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isReadDay) WarmForestGreen else PaperCreamBorder,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isReadDay) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Read on this day",
                                            tint = WarmForestGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(PaperCreamBorder, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                    Text(
                        text = "Every sentence you read is a step forward. Take breaks whenever you need!",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // ==========================================
            // 2. WARM PRAISE BANNER
            // ==========================================
            uiState.praiseMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(DesignTokens.ElevationAndBorders.borderThin, WarmForestGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmForestGreenContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(DesignTokens.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = WarmForestGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
            }

            // ==========================================
            // 3. FALLBACK READ-ALONG TTS BANNER (If speech recognizer unavailable)
            // ==========================================
            if (uiState.isTtsFallbackActive || !uiState.isSpeechRecognitionAvailable) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(DesignTokens.ElevationAndBorders.borderThin, TerracottaAccent.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(DesignTokens.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Headphones, contentDescription = null, tint = TerracottaAccent)
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))
                            Column {
                                Text(
                                    text = "Read-Along Mode",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CharcoalTextPrimary
                                )
                                Text(
                                    text = "Speech recognition unavailable. Tap Listen to read along with audio.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CharcoalTextSecondary
                                )
                            }
                        }
                        Button(
                            onClick = viewModel::playSentenceTts,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Listen", color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
            }

            // ==========================================
            // 4. FOCUS LINE READING PASSAGE CANVAS
            // ==========================================
            val sentences = uiState.parsedText?.sentences ?: emptyList()
            if (sentences.isNotEmpty()) {
                val currentIndex = uiState.currentSentenceIndex
                val prevSentence = sentences.getOrNull(currentIndex - 1)
                val currentSentence = sentences.getOrNull(currentIndex)
                val nextSentence = sentences.getOrNull(currentIndex + 1)

                Text(
                    text = "Line ${currentIndex + 1} of ${sentences.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalTextSecondary
                )
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(DesignTokens.ElevationAndBorders.borderThick, TerracottaAccent.copy(alpha = 0.45f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
                ) {
                    Column(modifier = Modifier.padding(DesignTokens.Spacing.lg)) {

                        // Context: Dimmed Previous Sentence
                        if (prevSentence != null) {
                            Text(
                                text = prevSentence.sentence,
                                fontSize = 16.sp,
                                color = CharcoalTextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = DesignTokens.Spacing.sm)
                                    .clickable { viewModel.prevSentence() }
                            )
                        }

                        // --- ACTIVE FOCUS LINE READING RULER GUIDE ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TerracottaAccent.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
                                .border(1.5.dp, TerracottaAccent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.md)
                        ) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentSentence?.words?.forEach { wordSpan ->
                                    val isMatched = uiState.matchedWords.contains(wordSpan.word)
                                    val bgColor = if (isMatched) WarmForestGreenContainer else PaperCreamBackground
                                    val borderColor = if (isMatched) WarmForestGreen else PaperCreamBorder

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = bgColor,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                                        modifier = Modifier.clickable {
                                            viewModel.speakWordNormally(wordSpan.cleanWord)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = wordSpan.word,
                                                fontSize = 24.sp,
                                                fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isMatched) WarmForestGreen else CharcoalTextPrimary
                                            )
                                            if (isMatched) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Read successfully",
                                                    tint = WarmForestGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Context: Dimmed Next Sentence
                        if (nextSentence != null) {
                            Text(
                                text = nextSentence.sentence,
                                fontSize = 16.sp,
                                color = CharcoalTextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = DesignTokens.Spacing.sm)
                                    .clickable { viewModel.nextSentence() }
                            )
                        }

                        // Live Spoken Transcription Subtitle
                        if (uiState.spokenTranscription.isNotBlank() || uiState.isListening) {
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CalmTealContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = DesignTokens.Spacing.sm, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = CalmTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                                Text(
                                    text = if (uiState.spokenTranscription.isNotBlank()) "Heard: \"${uiState.spokenTranscription}\"" else "Listening to you read...",
                                    fontSize = 13.sp,
                                    color = CalmTeal,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

                        // Mic / Reading Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Line
                            IconButton(
                                onClick = viewModel::prevSentence,
                                enabled = currentIndex > 0
                            ) {
                                Icon(
                                    Icons.Default.SkipPrevious,
                                    contentDescription = "Previous Line",
                                    tint = if (currentIndex > 0) CharcoalTextPrimary else CharcoalTextSecondary.copy(alpha = 0.3f)
                                )
                            }

                            // Big Gentle Mic Button with pulsing indicator
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = if (uiState.isListening) 1.06f else 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "mic_pulse"
                            )

                            Button(
                                onClick = {
                                    if (!hasMicPermission) {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        if (uiState.isListening) {
                                            viewModel.stopListening()
                                        } else {
                                            viewModel.startListening()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isListening) TerracottaAccent else CalmTeal
                                ),
                                modifier = Modifier
                                    .height(52.dp)
                                    .scale(pulseScale)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                                Text(
                                    text = if (uiState.isListening) "Listening to you..." else "Read This Line",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Next Line
                            IconButton(
                                onClick = viewModel::nextSentence,
                                enabled = currentIndex < sentences.size - 1
                            ) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "Next Line",
                                    tint = if (currentIndex < sentences.size - 1) CharcoalTextPrimary else CharcoalTextSecondary.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

            // ==========================================
            // 5. "WORDS TO TRY AGAIN" COLLECTION (Gentle, No Red Marks)
            // ==========================================
            if (uiState.wordsToTryAgain.isNotEmpty()) {
                val script = uiState.parsedText?.script ?: SupportedScript.LATIN

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(DesignTokens.ElevationAndBorders.borderThin, PaperCreamBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
                ) {
                    Column(modifier = Modifier.padding(DesignTokens.Spacing.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = TerracottaAccent)
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                            Text(
                                text = "Words to try again (${uiState.wordsToTryAgain.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                        }
                        Text(
                            text = "Tap to hear syllables slowly, or practice repeating aloud:",
                            style = MaterialTheme.typography.bodySmall,
                            color = CharcoalTextSecondary
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                        uiState.wordsToTryAgain.forEach { word ->
                            val syllables = PhonicsSyllableUtil.breakIntoSyllables(word, script.primaryLocale)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(PaperCreamBackground, shape = MaterialTheme.shapes.small)
                                    .border(0.5.dp, PaperCreamBorder, shape = MaterialTheme.shapes.small)
                                    .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.sm)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = word,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CharcoalTextPrimary
                                        )

                                        // Syllable Chips Breakdown
                                        if (syllables.size > 1) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                syllables.forEachIndexed { sIdx, syl ->
                                                    val sylBg = if (sIdx % 2 == 0) CalmTealContainer else TerracottaAccentContainer
                                                    val sylColor = if (sIdx % 2 == 0) CalmTeal else TerracottaAccent
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = sylBg
                                                    ) {
                                                        Text(
                                                            text = syl,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = sylColor,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Hear Word Normally
                                        IconButton(onClick = { viewModel.speakWordNormally(word) }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Hear word",
                                                tint = CalmTeal
                                            )
                                        }

                                        // Hear Slow Syllables
                                        IconButton(onClick = { viewModel.speakWordSlowly(word) }) {
                                            Icon(
                                                imageVector = Icons.Default.Spa,
                                                contentDescription = "Hear syllables slowly",
                                                tint = TerracottaAccent
                                            )
                                        }

                                        // Open Interactive Repeat Exercise Modal
                                        Button(
                                            onClick = { viewModel.openRepeatModal(word) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CalmTeal),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Practice", fontSize = 12.sp, color = Color.White)
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Direct Master Word Button
                                        IconButton(onClick = { viewModel.practiceSingleWord(word) }) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Mark mastered",
                                                tint = WarmForestGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xxl))
        }
    }

    // ==========================================
    // 6. SHORT REPEAT EXERCISE MODAL
    // ==========================================
    if (uiState.isRepeatModalOpen && uiState.activeRepeatWord != null) {
        val word = uiState.activeRepeatWord!!
        val isListeningThis = uiState.isListening && uiState.listeningForWord == word

        Dialog(onDismissRequest = viewModel::closeRepeatModal) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, TerracottaAccent.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Practice Word",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )
                        IconButton(onClick = viewModel::closeRepeatModal) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = CharcoalTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                    // Big Target Word Display
                    Text(
                        text = word,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isRepeatSuccessful) WarmForestGreen else CharcoalTextPrimary
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))

                    // Syllable breakdown chips
                    if (uiState.repeatSyllables.size > 1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.repeatSyllables.forEachIndexed { sIdx, syl ->
                                val sylBg = if (sIdx % 2 == 0) CalmTealContainer else TerracottaAccentContainer
                                val sylColor = if (sIdx % 2 == 0) CalmTeal else TerracottaAccent
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = sylBg
                                ) {
                                    Text(
                                        text = syl,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = sylColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                    // Listen buttons row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.speakWordNormally(word) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = CalmTeal)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hear Word", color = CalmTeal)
                        }

                        OutlinedButton(
                            onClick = { viewModel.speakWordSlowly(word) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Spa, contentDescription = null, tint = TerracottaAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Slow Syllables", color = TerracottaAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

                    // Success or Prompt Message
                    if (uiState.isRepeatSuccessful) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarmForestGreenContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(DesignTokens.Spacing.md),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmForestGreen)
                                Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                                Text(
                                    text = "🌟 Wonderful! You said '$word'!",
                                    fontWeight = FontWeight.Bold,
                                    color = WarmForestGreen
                                )
                            }
                        }
                    } else if (uiState.spokenTranscription.isNotBlank()) {
                        Text(
                            text = "Heard: \"${uiState.spokenTranscription}\"",
                            fontSize = 13.sp,
                            color = CharcoalTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                    // Big Mic Button for repeat
                    if (uiState.isRepeatSuccessful) {
                        Button(
                            onClick = viewModel::closeRepeatModal,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmForestGreen),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Done & Mastered! 🎉", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (!hasMicPermission) {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (isListeningThis) {
                                        viewModel.stopListening()
                                    } else {
                                        viewModel.startListening(word)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListeningThis) TerracottaAccent else CalmTeal
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isListeningThis) Icons.Default.GraphicEq else Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                            Text(
                                text = if (isListeningThis) "Listening to you..." else "Tap & Say Word",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
