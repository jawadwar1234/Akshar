package com.akshar.dyslexiareader.ui.reader

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.domain.model.SentenceSpan
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.model.WordSpan
import com.akshar.dyslexiareader.domain.util.PhonicsSyllableUtil
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.DyslexiaFontOption
import com.akshar.dyslexiareader.ui.theme.LocalReaderTheme
import com.akshar.dyslexiareader.ui.theme.ReaderThemePreset
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReaderScreen(
    documentId: String,
    viewModel: ReaderViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (String, String) -> Unit
) {
    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val ttsState by viewModel.ttsState.collectAsState()
    val theme = ReaderThemePreset.fromId(userSettings.readerTheme)

    CompositionLocalProvider(LocalReaderTheme provides theme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.document?.title ?: stringResource(R.string.reader_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.stopTts()
                            onNavigateBack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = theme.textPrimary
                            )
                        }
                    },
                    actions = {
                        // Reading Ruler Toggle with visible label
                        OutlinedButton(
                            onClick = viewModel::toggleRuler,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(end = DesignTokens.Spacing.xs)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LinearScale,
                                contentDescription = null,
                                tint = if (uiState.isRulerActive) TerracottaAccent else theme.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                            Text(
                                text = stringResource(R.string.reader_btn_ruler),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.isRulerActive) TerracottaAccent else theme.textPrimary
                            )
                        }

                        // Text Settings
                        IconButton(onClick = viewModel::openTextSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.reader_btn_appearance),
                                tint = theme.textPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.background)
                )
            },
            bottomBar = {
                // Read-Aloud Bottom Control Bar
                ReaderBottomBar(
                    ttsState = ttsState,
                    theme = theme,
                    onPlay = viewModel::playTts,
                    onPause = viewModel::pauseTts,
                    onStop = viewModel::stopTts,
                    onPrev = viewModel::prevSentence,
                    onNext = viewModel::nextSentence
                )
            },
            containerColor = theme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(theme.background)
            ) {
                // Reading Canvas
                val parsed = uiState.parsedText
                val fontOption = DyslexiaFontOption.fromId(userSettings.fontFamily)

                if (parsed != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = DesignTokens.Spacing.lg, vertical = DesignTokens.Spacing.md)
                    ) {
                        itemsIndexed(parsed.sentences) { sIdx, sentenceSpan ->
                            val isSentenceActive = ttsState.isPlaying && ttsState.currentSentenceIndex == sIdx

                            SentenceBlock(
                                sentenceSpan = sentenceSpan,
                                isSentenceActive = isSentenceActive,
                                activeWordCharStart = if (isSentenceActive) ttsState.currentWordCharStart else -1,
                                activeWordCharLength = ttsState.currentWordCharLength,
                                theme = theme,
                                fontSizeSp = userSettings.fontSize,
                                lineSpacing = userSettings.lineSpacing,
                                letterSpacingEm = userSettings.letterSpacing,
                                fontOption = fontOption,
                                script = parsed.script,
                                bionicEnabled = userSettings.bionicEnabled,
                                syllablesEnabled = userSettings.syllablesEnabled,
                                onWordClicked = { wordSpan -> viewModel.inspectWord(wordSpan) },
                                onSentenceClick = { viewModel.jumpToSentence(sIdx) }
                            )
                            Spacer(modifier = Modifier.height((userSettings.fontSize * (userSettings.lineSpacing - 1f)).dp))
                        }
                    }
                }

                // Reading Ruler Overlay if toggled
                if (uiState.isRulerActive) {
                    ReadingRulerOverlay()
                }
            }
        }

        // Word Inspection Dialog
        if (uiState.selectedWordForInspection != null) {
            WordInspectionDialog(
                wordSpan = uiState.selectedWordForInspection!!,
                onSpeakWord = { w -> viewModel.ttsManager.speakWord(w) },
                onPracticeWord = { cleanWord ->
                    val docId = uiState.document?.id ?: ""
                    onNavigateToPractice(docId, cleanWord)
                },
                onDismiss = viewModel::dismissWordInspection
            )
        }

        // Text Customizer Bottom Sheet
        if (uiState.isTextSettingsSheetOpen) {
            TextSettingsBottomSheet(
                userSettings = userSettings,
                onDismiss = viewModel::closeTextSettings,
                onUpdateTheme = viewModel::updateTheme,
                onUpdateFontSize = viewModel::updateFontSize,
                onUpdateLineSpacing = viewModel::updateLineSpacing,
                onUpdateLetterSpacing = viewModel::updateLetterSpacing,
                onUpdateFontFamily = viewModel::updateFontFamily,
                onToggleBionic = viewModel::toggleBionicReading,
                onToggleSyllables = viewModel::toggleSyllableBreaks,
                onUpdateTtsSpeed = viewModel::updateTtsSpeed,
                onUpdateTtsPitch = viewModel::updateTtsPitch
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SentenceBlock(
    sentenceSpan: SentenceSpan,
    isSentenceActive: Boolean,
    activeWordCharStart: Int,
    activeWordCharLength: Int,
    theme: ReaderThemePreset,
    fontSizeSp: Float,
    lineSpacing: Float,
    letterSpacingEm: Float,
    fontOption: DyslexiaFontOption,
    script: SupportedScript,
    bionicEnabled: Boolean,
    syllablesEnabled: Boolean,
    onWordClicked: (WordSpan) -> Unit,
    onSentenceClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSentenceActive) theme.highlightSentenceBg else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .clickable { onSentenceClick() }
            .padding(DesignTokens.Spacing.xs)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            sentenceSpan.words.forEach { wordSpan ->
                val isWordActive = isSentenceActive && 
                    activeWordCharStart >= 0 &&
                    wordSpan.startIndex <= activeWordCharStart &&
                    (wordSpan.startIndex + wordSpan.word.length) >= activeWordCharStart

                WordChip(
                    wordSpan = wordSpan,
                    isWordActive = isWordActive,
                    theme = theme,
                    fontSizeSp = fontSizeSp,
                    letterSpacingEm = letterSpacingEm,
                    fontOption = fontOption,
                    script = script,
                    bionicEnabled = bionicEnabled,
                    syllablesEnabled = syllablesEnabled,
                    onClick = { onWordClicked(wordSpan) }
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}

@Composable
private fun WordChip(
    wordSpan: WordSpan,
    isWordActive: Boolean,
    theme: ReaderThemePreset,
    fontSizeSp: Float,
    letterSpacingEm: Float,
    fontOption: DyslexiaFontOption,
    script: SupportedScript,
    bionicEnabled: Boolean,
    syllablesEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isWordActive) theme.highlightWordBg else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 1.dp)
    ) {
        if (syllablesEnabled && wordSpan.syllables.size > 1) {
            // Syllable separated display: e.g. "read · ing"
            Text(
                text = wordSpan.syllables.joinToString("·"),
                fontSize = fontSizeSp.sp,
                fontFamily = fontOption.fontFamily,
                color = theme.textPrimary,
                letterSpacing = letterSpacingEm.sp,
                textAlign = TextAlign.Start
            )
        } else if (bionicEnabled) {
            // Bionic fixation display: bold initial 40% letters
            val (prefix, suffix) = PhonicsSyllableUtil.splitForBionicFocus(wordSpan.word, script.primaryLocale)
            val annotated = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = theme.textPrimary)) {
                    append(prefix)
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = theme.textPrimary)) {
                    append(suffix)
                }
            }
            Text(
                text = annotated,
                fontSize = fontSizeSp.sp,
                fontFamily = fontOption.fontFamily,
                letterSpacing = letterSpacingEm.sp,
                textAlign = TextAlign.Start
            )
        } else {
            // Standard left-aligned dyslexia text
            Text(
                text = wordSpan.word,
                fontSize = fontSizeSp.sp,
                fontFamily = fontOption.fontFamily,
                fontWeight = FontWeight.Normal,
                color = theme.textPrimary,
                letterSpacing = letterSpacingEm.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun ReaderBottomBar(
    ttsState: TtsPlaybackState,
    theme: ReaderThemePreset,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = DesignTokens.ElevationAndBorders.borderThin,
                color = theme.borderColor,
                shape = RectangleShape
            ),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = theme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Sentence
            OutlinedButton(
                onClick = onPrev,
                modifier = Modifier.height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = theme.textPrimary)
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(
                    text = stringResource(R.string.reader_btn_prev_sentence),
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.textPrimary
                )
            }

            // Play / Pause Main Button
            Button(
                onClick = {
                    if (ttsState.isPlaying) onPause() else onPlay()
                },
                modifier = Modifier.height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
            ) {
                Icon(
                    imageVector = if (ttsState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                Text(
                    text = if (ttsState.isPlaying) stringResource(R.string.reader_btn_pause) else stringResource(R.string.reader_btn_play),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Next Sentence
            OutlinedButton(
                onClick = onNext,
                modifier = Modifier.height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(R.string.reader_btn_next_sentence),
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.textPrimary
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Icon(Icons.Default.SkipNext, contentDescription = null, tint = theme.textPrimary)
            }
        }
    }
}
