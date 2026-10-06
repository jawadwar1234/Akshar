package com.akshar.dyslexiareader.ui.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.domain.model.WordSpan
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
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
    }

    LaunchedEffect(documentId) {
        viewModel.loadPracticeData(documentId, targetWord)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.practice_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = CharcoalTextPrimary
                        )
                    }
                },
                actions = {
                    // Word Bank Button with visible label
                    OutlinedButton(
                        onClick = onNavigateToWordMastery,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(end = DesignTokens.Spacing.sm)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = TerracottaAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                        Text("Word Bank", style = MaterialTheme.typography.labelMedium, color = TerracottaAccent)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

            Text(
                text = stringResource(R.string.practice_instruction),
                style = MaterialTheme.typography.bodyLarge,
                color = CharcoalTextSecondary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // Current Sentence Reading Card
            val currentSentence = uiState.parsedText?.sentences?.getOrNull(uiState.currentSentenceIndex)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = DesignTokens.ElevationAndBorders.borderThin,
                        color = PaperCreamBorder,
                        shape = MaterialTheme.shapes.large
                    ),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.lg),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (currentSentence != null) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentSentence.words.forEach { wordSpan ->
                                val isSelected = uiState.currentTargetWord?.startIndex == wordSpan.startIndex
                                val isMastered = uiState.matchedWords.contains(wordSpan.word)

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when {
                                                isMastered -> WarmForestGreenContainer
                                                isSelected -> TerracottaAccent.copy(alpha = 0.18f)
                                                else -> Color.Transparent
                                            },
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = when {
                                                isMastered -> WarmForestGreen
                                                isSelected -> TerracottaAccent
                                                else -> PaperCreamBorder
                                            },
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .clickable { viewModel.selectTargetWord(wordSpan) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = wordSpan.word,
                                        fontSize = 24.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isMastered) WarmForestGreen else CharcoalTextPrimary
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Loading practice sentence…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // Target Word Card with Pronunciation button
            val target = uiState.currentTargetWord
            if (target != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = DesignTokens.ElevationAndBorders.borderThin,
                            color = CalmTeal.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        ),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = PaperCreamSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Word:",
                                style = MaterialTheme.typography.labelMedium,
                                color = CharcoalTextSecondary
                            )
                            Text(
                                text = target.word,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalTextPrimary
                            )
                            if (target.syllables.isNotEmpty()) {
                                Text(
                                    text = target.syllables.joinToString(" · "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CalmTeal
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.speakTargetWord(target.cleanWord) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(CalmTeal.copy(alpha = 0.12f), shape = CircleShape)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = CalmTeal)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            // Praise / Feedback Banner
            if (uiState.praiseMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = DesignTokens.ElevationAndBorders.borderThin,
                            color = WarmForestGreen.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.medium
                        ),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = WarmForestGreenContainer)
                ) {
                    Text(
                        text = uiState.praiseMessage ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmForestGreen,
                        modifier = Modifier.padding(DesignTokens.Spacing.md)
                    )
                }
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
            }

            // Big Microphone Button
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        color = if (uiState.isListening) CalmTeal else TerracottaAccent,
                        shape = CircleShape
                    )
                    .clickable {
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
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
            Text(
                text = if (uiState.isListening) stringResource(R.string.practice_btn_mic_listening) else stringResource(R.string.practice_btn_mic_start),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (uiState.isListening) CalmTeal else TerracottaAccent
            )

            if (uiState.spokenTranscription.isNotBlank()) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                Text(
                    text = "Heard: \"${uiState.spokenTranscription}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.lg))

            // Navigation between sentences
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = viewModel::prevSentence,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.height(DesignTokens.buttonHeight)
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = CharcoalTextPrimary)
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text(stringResource(R.string.reader_btn_prev_sentence), color = CharcoalTextPrimary)
                }

                OutlinedButton(
                    onClick = viewModel::nextSentence,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.height(DesignTokens.buttonHeight)
                ) {
                    Text(stringResource(R.string.reader_btn_next_sentence), color = CharcoalTextPrimary)
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Icon(Icons.Default.SkipNext, contentDescription = null, tint = CharcoalTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
        }
    }
}
