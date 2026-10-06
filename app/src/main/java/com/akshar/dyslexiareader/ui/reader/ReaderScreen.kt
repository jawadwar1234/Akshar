package com.akshar.dyslexiareader.ui.reader

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.domain.model.SentenceSpan
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.model.UserSettings
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReaderScreen(
    documentId: String,
    viewModel: ReaderViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (String, String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    // Clean pause on backgrounding or phone calls
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.pauseTts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopTts()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val ttsState by viewModel.ttsState.collectAsState()
    val theme = ReaderThemePreset.fromId(userSettings.readerTheme)

    CompositionLocalProvider(LocalReaderTheme provides theme) {
        Scaffold(
            topBar = {
                if (!uiState.isFullScreen) {
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
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.action_back),
                                    tint = theme.textPrimary
                                )
                            }
                        },
                        actions = {
                            // Before/After Photo Toggle
                            IconButton(onClick = viewModel::toggleBeforeAfterSplit) {
                                Icon(
                                    imageVector = Icons.Default.Compare,
                                    contentDescription = "Compare original photo",
                                    tint = if (uiState.isBeforeAfterSplit) TerracottaAccent else theme.textPrimary
                                )
                            }

                            // Edit Mode Toggle
                            IconButton(onClick = viewModel::toggleEditMode) {
                                Icon(
                                    imageVector = if (uiState.isEditMode) Icons.Default.Save else Icons.Default.Edit,
                                    contentDescription = if (uiState.isEditMode) "Save text edits" else "Edit text",
                                    tint = if (uiState.isEditMode) TerracottaAccent else theme.textPrimary
                                )
                            }

                            // Reading Ruler Toggle
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

                            // Full-screen Toggle
                            IconButton(onClick = viewModel::toggleFullScreen) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Full-screen mode",
                                    tint = theme.textPrimary
                                )
                            }

                            // Appearance Settings
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
                }
            },
            bottomBar = {
                if (!uiState.isFullScreen && !uiState.isEditMode) {
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
                }
            },
            containerColor = theme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 0.dp,
                        top = if (uiState.isFullScreen) 0.dp else padding.calculateTopPadding(),
                        end = 0.dp,
                        bottom = if (uiState.isFullScreen) 0.dp else padding.calculateBottomPadding()
                    )
                    .background(theme.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Missing Voice Banner if voice not installed on device
                    if (!ttsState.isVoiceAvailable) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.md)
                                .border(1.dp, TerracottaAccent.copy(alpha = 0.4f), MaterialTheme.shapes.medium),
                            colors = CardDefaults.cardColors(containerColor = theme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(DesignTokens.Spacing.md),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Voice data needed for ${ttsState.missingLanguageName ?: "this language"}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalTextPrimary
                                    )
                                    Text(
                                        text = "Install the voice pack to listen to this book page aloud.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CharcoalTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                                OutlinedButton(
                                    onClick = { viewModel.ttsManager.openTtsSettings() },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Install", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (uiState.isEditMode) {
                            // Edit Mode view
                            EditModeCanvas(
                                draftText = uiState.editableDraftText,
                                onTextChange = viewModel::updateDraftText,
                                onSave = viewModel::saveEditedText,
                                onCancel = viewModel::toggleEditMode,
                                theme = theme
                            )
                        } else if (uiState.isBeforeAfterSplit && uiState.document?.imagePath != null) {
                            // Before / After side-by-side view
                            Row(modifier = Modifier.fillMaxSize()) {
                                // Original Photo Side
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(Color.Black.copy(alpha = 0.05f))
                                        .border(1.dp, theme.borderColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val imageFile = remember(uiState.document?.imagePath) {
                                        File(uiState.document!!.imagePath!!)
                                    }
                                    if (imageFile.exists()) {
                                        val bitmap = remember(imageFile) {
                                            BitmapFactory.decodeFile(imageFile.absolutePath)
                                        }
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Original textbook page photo",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize().padding(DesignTokens.Spacing.xs)
                                            )
                                        }
                                    }
                                }

                                // Dyslexia Text Side
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    ReaderTextCanvas(
                                        uiState = uiState,
                                        userSettings = userSettings,
                                        ttsState = ttsState,
                                        theme = theme,
                                        onWordClicked = { wordSpan -> viewModel.inspectWord(wordSpan) },
                                        onWordLongClicked = { wordSpan ->
                                            val script = uiState.parsedText?.script ?: SupportedScript.LATIN
                                            viewModel.ttsManager.speakWordSlowlySyllables(wordSpan.cleanWord, script)
                                        },
                                        onSentenceClick = { sIdx -> viewModel.jumpToSentence(sIdx) }
                                    )
                                }
                            }
                        } else {
                            // Standard Dyslexia Reading Canvas
                            ReaderTextCanvas(
                                uiState = uiState,
                                userSettings = userSettings,
                                ttsState = ttsState,
                                theme = theme,
                                onWordClicked = { wordSpan -> viewModel.inspectWord(wordSpan) },
                                onWordLongClicked = { wordSpan ->
                                    val script = uiState.parsedText?.script ?: SupportedScript.LATIN
                                    viewModel.ttsManager.speakWordSlowlySyllables(wordSpan.cleanWord, script)
                                },
                                onSentenceClick = { sIdx -> viewModel.jumpToSentence(sIdx) }
                            )
                        }

                        // Reading Ruler Overlay if toggled
                        if (uiState.isRulerActive && !uiState.isEditMode) {
                            ReadingRulerOverlay(
                                windowLines = userSettings.rulerWindowLines
                            )
                        }

                        // Minimal floating controls when in Full-Screen mode
                        if (uiState.isFullScreen) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(DesignTokens.Spacing.lg),
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm)
                            ) {
                                FloatingActionButton(
                                    onClick = { if (ttsState.isPlaying) viewModel.pauseTts() else viewModel.playTts() },
                                    containerColor = TerracottaAccent,
                                    contentColor = Color.White
                                ) {
                                    Icon(
                                        imageVector = if (ttsState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause"
                                    )
                                }

                                FloatingActionButton(
                                    onClick = viewModel::openTextSettings,
                                    containerColor = theme.surface,
                                    contentColor = theme.textPrimary
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                                }

                                FloatingActionButton(
                                    onClick = viewModel::toggleFullScreen,
                                    containerColor = theme.surface,
                                    contentColor = theme.textPrimary
                                ) {
                                    Icon(Icons.Default.FullscreenExit, contentDescription = "Exit full screen")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Word Inspection Dialog
        if (uiState.selectedWordForInspection != null) {
            val script = uiState.parsedText?.script ?: SupportedScript.LATIN
            WordInspectionDialog(
                wordSpan = uiState.selectedWordForInspection!!,
                onSpeakWord = { w -> viewModel.ttsManager.speakWord(w, script) },
                onSpeakWordSlowly = { w -> viewModel.ttsManager.speakWordSlowlySyllables(w, script) },
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
                onUpdateWordSpacing = viewModel::updateWordSpacing,
                onUpdateMarginWidth = viewModel::updateMarginWidth,
                onUpdateFontFamily = viewModel::updateFontFamily,
                onUpdateRulerWindow = viewModel::updateRulerWindowLines,
                onUpdateChunkingPalette = viewModel::updateChunkingPaletteIndex,
                onToggleBionic = viewModel::toggleBionicReading,
                onToggleSyllables = viewModel::toggleSyllableBreaks,
                onUpdateTtsSpeed = viewModel::updateTtsSpeed,
                onUpdateTtsPitch = viewModel::updateTtsPitch
            )
        }
    }
}

@Composable
private fun ReaderTextCanvas(
    uiState: ReaderUiState,
    userSettings: UserSettings,
    ttsState: TtsPlaybackState,
    theme: ReaderThemePreset,
    onWordClicked: (WordSpan) -> Unit,
    onWordLongClicked: (WordSpan) -> Unit,
    onSentenceClick: (Int) -> Unit
) {
    val parsed = uiState.parsedText ?: return
    val fontOption = resolveFontForScript(userSettings.fontFamily, parsed.script)
    val (chunkColor1, chunkColor2) = PhonicsSyllableUtil.getChunkPalette(userSettings.chunkingPaletteIndex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = userSettings.marginWidth.dp, vertical = DesignTokens.Spacing.md)
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
                wordSpacingEm = userSettings.wordSpacing,
                fontOption = fontOption,
                script = parsed.script,
                bionicEnabled = userSettings.bionicEnabled,
                syllablesEnabled = userSettings.syllablesEnabled,
                chunkColor1 = chunkColor1,
                chunkColor2 = chunkColor2,
                onWordClicked = onWordClicked,
                onWordLongClicked = onWordLongClicked,
                onSentenceClick = { onSentenceClick(sIdx) }
            )
            Spacer(modifier = Modifier.height((userSettings.fontSize * (userSettings.lineSpacing - 1f)).dp))
        }
    }
}

private fun resolveFontForScript(
    selectedFontId: String,
    script: SupportedScript
): DyslexiaFontOption {
    return when (script) {
        SupportedScript.DEVANAGARI -> DyslexiaFontOption.NOTO_SANS_DEVANAGARI
        SupportedScript.GUJARATI -> DyslexiaFontOption.NOTO_SANS_GUJARATI
        SupportedScript.BENGALI -> DyslexiaFontOption.NOTO_SANS_BENGALI
        SupportedScript.LATIN -> DyslexiaFontOption.fromId(selectedFontId)
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
    wordSpacingEm: Float,
    fontOption: DyslexiaFontOption,
    script: SupportedScript,
    bionicEnabled: Boolean,
    syllablesEnabled: Boolean,
    chunkColor1: Color,
    chunkColor2: Color,
    onWordClicked: (WordSpan) -> Unit,
    onWordLongClicked: (WordSpan) -> Unit,
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
                    chunkColor1 = chunkColor1,
                    chunkColor2 = chunkColor2,
                    onClick = { onWordClicked(wordSpan) },
                    onLongClick = { onWordLongClicked(wordSpan) }
                )
                Spacer(modifier = Modifier.width((6 + wordSpacingEm * 20).dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
    chunkColor1: Color,
    chunkColor2: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isWordActive) theme.highlightWordBg else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 2.dp, vertical = 1.dp)
    ) {
        if (syllablesEnabled && wordSpan.syllables.size > 1) {
            val annotated = buildAnnotatedString {
                wordSpan.syllables.forEachIndexed { idx, syllable ->
                    val color = if (idx % 2 == 0) chunkColor1 else chunkColor2
                    withStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold)) {
                        append(syllable)
                    }
                }
            }
            Text(
                text = annotated,
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * 1.6f).sp,
                fontFamily = fontOption.fontFamily,
                letterSpacing = letterSpacingEm.sp,
                textAlign = TextAlign.Start
            )
        } else if (bionicEnabled) {
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
                lineHeight = (fontSizeSp * 1.6f).sp,
                fontFamily = fontOption.fontFamily,
                letterSpacing = letterSpacingEm.sp,
                textAlign = TextAlign.Start
            )
        } else {
            Text(
                text = wordSpan.word,
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * 1.6f).sp,
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
private fun EditModeCanvas(
    draftText: String,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    theme: ReaderThemePreset
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignTokens.Spacing.lg)
    ) {
        Text(
            text = "Edit Scanned Text",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )
        Text(
            text = "Fix any misspelled words or OCR mistakes below:",
            style = MaterialTheme.typography.bodyMedium,
            color = CharcoalTextSecondary
        )
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        OutlinedTextField(
            value = draftText,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = theme.textPrimary)
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(DesignTokens.buttonHeight)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f).height(DesignTokens.buttonHeight),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
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

            // Play / Pause Main Button ("Read To Me")
            Button(
                onClick = {
                    if (ttsState.isPlaying) onPause() else onPlay()
                },
                modifier = Modifier.height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
            ) {
                Icon(
                    imageVector = if (ttsState.isPlaying) Icons.Default.Pause else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                Text(
                    text = if (ttsState.isPlaying) stringResource(R.string.reader_btn_pause) else "Read to Me",
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
