package com.akshar.dyslexiareader.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.repository.UserPreferencesRepository
import com.akshar.dyslexiareader.domain.model.FormattedReaderText
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.model.UserSettings
import com.akshar.dyslexiareader.domain.model.WordSpan
import com.akshar.dyslexiareader.domain.repository.DocumentRepository
import com.akshar.dyslexiareader.domain.repository.WordMasteryRepository
import com.akshar.dyslexiareader.domain.util.UnicodeIndicTextUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReaderUiState(
    val document: DocumentEntity? = null,
    val parsedText: FormattedReaderText? = null,
    val activeSentenceIndex: Int = 0,
    val activeWordCharStart: Int = -1,
    val activeWordCharLength: Int = 0,
    val selectedWordForInspection: WordSpan? = null,
    val isTextSettingsSheetOpen: Boolean = false,
    val isRulerActive: Boolean = false
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val wordMasteryRepository: WordMasteryRepository,
    private val preferencesRepository: UserPreferencesRepository,
    val ttsManager: TextToSpeechManager
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = preferencesRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val ttsState: StateFlow<TtsPlaybackState> = ttsManager.playbackState

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            val doc = documentRepository.getDocumentById(documentId)
            if (doc != null) {
                val parsed = UnicodeIndicTextUtil.parseReaderText(doc.cleanText)
                _uiState.value = _uiState.value.copy(
                    document = doc,
                    parsedText = parsed
                )

                // Prepare TTS with sentences and offsets
                val sentences = parsed.sentences.map { it.sentence }
                val offsets = parsed.sentences.map { it.startIndex }
                ttsManager.prepareText(sentences, offsets, parsed.script)
            }
        }
    }

    fun playTts() {
        ttsManager.play()
    }

    fun pauseTts() {
        ttsManager.pause()
    }

    fun stopTts() {
        ttsManager.stop()
    }

    fun nextSentence() {
        ttsManager.nextSentence()
    }

    fun prevSentence() {
        ttsManager.prevSentence()
    }

    fun jumpToSentence(index: Int) {
        ttsManager.jumpToSentence(index)
    }

    fun toggleRuler() {
        val willActive = !_uiState.value.isRulerActive
        _uiState.value = _uiState.value.copy(isRulerActive = willActive)
        viewModelScope.launch {
            preferencesRepository.updateReaderPreferences(rulerEnabled = willActive)
        }
    }

    fun openTextSettings() {
        _uiState.value = _uiState.value.copy(isTextSettingsSheetOpen = true)
    }

    fun closeTextSettings() {
        _uiState.value = _uiState.value.copy(isTextSettingsSheetOpen = false)
    }

    fun updateTheme(themeId: String) {
        viewModelScope.launch {
            preferencesRepository.updateReaderPreferences(theme = themeId)
        }
    }

    fun updateFontSize(sizeSp: Float) {
        viewModelScope.launch {
            preferencesRepository.updateReaderPreferences(fontSize = sizeSp)
        }
    }

    fun updateLineSpacing(spacing: Float) {
        viewModelScope.launch {
            preferencesRepository.updateReaderPreferences(lineSpacing = spacing)
        }
    }

    fun updateLetterSpacing(spacing: Float) {
        viewModelScope.launch {
            preferencesRepository.updateReaderPreferences(letterSpacing = spacing)
        }
    }

    fun updateFontFamily(fontId: String) {
        viewModelScope.launch {
            preferencesRepository.updateReaderPreferences(fontFamily = fontId)
        }
    }

    fun toggleBionicReading() {
        viewModelScope.launch {
            val current = userSettings.value.bionicEnabled
            preferencesRepository.updateReaderPreferences(bionicEnabled = !current)
        }
    }

    fun toggleSyllableBreaks() {
        viewModelScope.launch {
            val current = userSettings.value.syllablesEnabled
            preferencesRepository.updateReaderPreferences(syllablesEnabled = !current)
        }
    }

    fun updateTtsSpeed(speed: Float) {
        ttsManager.setSpeechRate(speed)
        viewModelScope.launch {
            preferencesRepository.updateReaderPreferences(ttsSpeed = speed)
        }
    }

    fun updateTtsPitch(pitch: Float) {
        ttsManager.setSpeechPitch(pitch)
        viewModelScope.launch {
            preferencesRepository.updateReaderPreferences(ttsPitch = pitch)
        }
    }

    fun inspectWord(word: WordSpan) {
        _uiState.value = _uiState.value.copy(selectedWordForInspection = word)
        val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
        ttsManager.speakWord(word.cleanWord, script)

        // Record word read in background
        val doc = _uiState.value.document
        if (doc != null) {
            viewModelScope.launch {
                wordMasteryRepository.recordWordRead(doc.childProfileId, word.word, script.name)
            }
        }
    }

    fun dismissWordInspection() {
        _uiState.value = _uiState.value.copy(selectedWordForInspection = null)
    }

    fun updateReadingProgress(progressPercent: Int, lastReadWordIndex: Int) {
        val doc = _uiState.value.document ?: return
        viewModelScope.launch {
            documentRepository.updateProgress(doc.id, progressPercent, lastReadWordIndex)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }
}
