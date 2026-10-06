package com.akshar.dyslexiareader.ui.practice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity
import com.akshar.dyslexiareader.domain.model.FormattedReaderText
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.model.WordSpan
import com.akshar.dyslexiareader.domain.repository.DocumentRepository
import com.akshar.dyslexiareader.domain.repository.WordMasteryRepository
import com.akshar.dyslexiareader.domain.util.UnicodeIndicTextUtil
import com.akshar.dyslexiareader.domain.util.WordMatcher
import com.akshar.dyslexiareader.ui.reader.TextToSpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PracticeUiState(
    val document: DocumentEntity? = null,
    val parsedText: FormattedReaderText? = null,
    val currentSentenceIndex: Int = 0,
    val currentTargetWord: WordSpan? = null,
    val isListening: Boolean = false,
    val spokenTranscription: String = "",
    val matchedWords: Set<String> = emptySet(),
    val praiseMessage: String? = null,
    val isWordMastered: Boolean = false,
    val hasMicPermission: Boolean = false
)

@HiltViewModel
class PracticeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository,
    private val wordMasteryRepository: WordMasteryRepository,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    fun loadPracticeData(documentId: String, initialTargetWord: String? = null) {
        viewModelScope.launch {
            val doc = documentRepository.getDocumentById(documentId)
            if (doc != null) {
                val parsed = UnicodeIndicTextUtil.parseReaderText(doc.cleanText)
                val targetWordSpan = if (!initialTargetWord.isNullOrBlank()) {
                    parsed.allWords.find { it.cleanWord == initialTargetWord.trim().lowercase() }
                        ?: parsed.allWords.firstOrNull()
                } else {
                    parsed.allWords.firstOrNull()
                }

                _uiState.value = _uiState.value.copy(
                    document = doc,
                    parsedText = parsed,
                    currentSentenceIndex = 0,
                    currentTargetWord = targetWordSpan,
                    matchedWords = emptySet(),
                    praiseMessage = null
                )
            }
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _uiState.value = _uiState.value.copy(isListening = true, praiseMessage = null)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _uiState.value = _uiState.value.copy(isListening = false)
                }

                override fun onError(error: Int) {
                    _uiState.value = _uiState.value.copy(isListening = false)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: ""
                    onSpeechRecognized(spokenText)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partialText = matches?.firstOrNull() ?: ""
                    if (partialText.isNotBlank()) {
                        _uiState.value = _uiState.value.copy(spokenTranscription = partialText)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, script.primaryLocale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isListening = false)
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _uiState.value = _uiState.value.copy(isListening = false)
    }

    private fun onSpeechRecognized(spokenText: String) {
        val targetWord = _uiState.value.currentTargetWord
        val targetSentence = _uiState.value.parsedText?.sentences?.getOrNull(_uiState.value.currentSentenceIndex)
        val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
        val profileId = _uiState.value.document?.childProfileId ?: "default"

        _uiState.value = _uiState.value.copy(
            spokenTranscription = spokenText,
            isListening = false
        )

        if (targetWord != null) {
            val isMatch = WordMatcher.isFuzzyMatch(spokenText, targetWord.cleanWord)
            if (isMatch) {
                val updatedMatches = _uiState.value.matchedWords + targetWord.word
                _uiState.value = _uiState.value.copy(
                    matchedWords = updatedMatches,
                    praiseMessage = "🌟 Wonderful reading! Great job!",
                    isWordMastered = true
                )
                viewModelScope.launch {
                    wordMasteryRepository.recordWordPractice(profileId, targetWord.word, script.name, wasSuccessful = true)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    praiseMessage = "Great effort! Let's try saying it together!"
                )
                viewModelScope.launch {
                    wordMasteryRepository.recordWordPractice(profileId, targetWord.word, script.name, wasSuccessful = false)
                }
            }
        } else if (targetSentence != null) {
            val targetWords = targetSentence.words.map { it.word }
            val matched = WordMatcher.findMatchedWordsInSentence(spokenText, targetWords)
            val updated = _uiState.value.matchedWords + matched

            val isComplete = updated.containsAll(targetWords)
            val praise = if (isComplete) "🌟 Outstanding! You read the whole line!" else "🌟 Great effort! Keep going!"
            _uiState.value = _uiState.value.copy(
                matchedWords = updated,
                praiseMessage = praise
            )

            viewModelScope.launch {
                matched.forEach { word ->
                    wordMasteryRepository.recordWordPractice(profileId, word, script.name, wasSuccessful = true)
                }
            }
        }
    }

    fun selectTargetWord(word: WordSpan) {
        _uiState.value = _uiState.value.copy(
            currentTargetWord = word,
            praiseMessage = null,
            spokenTranscription = ""
        )
        speakTargetWord(word.cleanWord)
    }

    fun speakTargetWord(word: String) {
        val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
        ttsManager.speakWord(word, script)
    }

    fun nextSentence() {
        val parsed = _uiState.value.parsedText ?: return
        val nextIdx = (_uiState.value.currentSentenceIndex + 1).coerceAtMost(parsed.sentences.size - 1)
        val sentence = parsed.sentences[nextIdx]
        _uiState.value = _uiState.value.copy(
            currentSentenceIndex = nextIdx,
            currentTargetWord = sentence.words.firstOrNull(),
            matchedWords = emptySet(),
            praiseMessage = null,
            spokenTranscription = ""
        )
    }

    fun prevSentence() {
        val parsed = _uiState.value.parsedText ?: return
        val prevIdx = (_uiState.value.currentSentenceIndex - 1).coerceAtLeast(0)
        val sentence = parsed.sentences[prevIdx]
        _uiState.value = _uiState.value.copy(
            currentSentenceIndex = prevIdx,
            currentTargetWord = sentence.words.firstOrNull(),
            matchedWords = emptySet(),
            praiseMessage = null,
            spokenTranscription = ""
        )
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
