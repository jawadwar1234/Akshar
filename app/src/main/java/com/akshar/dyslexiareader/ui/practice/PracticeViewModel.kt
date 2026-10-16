package com.akshar.dyslexiareader.ui.practice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.repository.UserPreferencesRepository
import com.akshar.dyslexiareader.domain.model.FormattedReaderText
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.model.WordSpan
import com.akshar.dyslexiareader.domain.repository.DocumentRepository
import com.akshar.dyslexiareader.domain.repository.WordMasteryRepository
import com.akshar.dyslexiareader.domain.util.PhonicsSyllableUtil
import com.akshar.dyslexiareader.domain.util.UnicodeIndicTextUtil
import com.akshar.dyslexiareader.domain.util.WordMatcher
import com.akshar.dyslexiareader.ui.reader.TextToSpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PracticeUiState(
    val document: DocumentEntity? = null,
    val parsedText: FormattedReaderText? = null,
    val currentSentenceIndex: Int = 0,
    val currentTargetWord: WordSpan? = null,
    val isListening: Boolean = false,
    val listeningForWord: String? = null,
    val activeRepeatWord: String? = null,
    val isRepeatModalOpen: Boolean = false,
    val repeatSyllables: List<String> = emptyList(),
    val isRepeatSuccessful: Boolean = false,
    val spokenTranscription: String = "",
    val matchedWords: Set<String> = emptySet(),
    val wordsToTryAgain: Set<String> = emptySet(),
    val praiseMessage: String? = null,
    val isWordMastered: Boolean = false,
    val isSpeechRecognitionAvailable: Boolean = true,
    val isTtsFallbackActive: Boolean = false,
    val isTtsPlaying: Boolean = false,
    val masteredWordsCount: Int = 0,
    val weeklyDaysRead: Int = 4,
    val weeklyDaysHistory: List<Boolean> = listOf(true, true, false, true, true, false, false) // Mon-Sun
)

@HiltViewModel
class PracticeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository,
    val wordMasteryRepository: WordMasteryRepository,
    val preferencesRepository: UserPreferencesRepository,
    val ttsManager: TextToSpeechManager
) : ViewModel() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _uiState = MutableStateFlow(
        PracticeUiState(
            isSpeechRecognitionAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        )
    )
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    fun loadPracticeData(documentId: String, initialTargetWord: String? = null) {
        viewModelScope.launch {
            val doc = documentRepository.getDocumentById(documentId)
            val profileId = doc?.childProfileId ?: preferencesRepository.userSettingsFlow.first().activeProfileId ?: "default"

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
                    wordsToTryAgain = if (!initialTargetWord.isNullOrBlank()) setOf(initialTargetWord.trim().lowercase()) else emptySet(),
                    praiseMessage = "🌟 Welcome! Read aloud at your own calm pace."
                )

                // Observe mastered count
                wordMasteryRepository.getMasteredCount(profileId).collect { count ->
                    _uiState.value = _uiState.value.copy(masteredWordsCount = count)
                }
            }
        }
    }

    fun startListening(targetWord: String? = null) {
        mainHandler.post {
            val isAvailable = try {
                SpeechRecognizer.isRecognitionAvailable(context)
            } catch (_: Exception) {
                false
            }

            if (!isAvailable) {
                _uiState.value = _uiState.value.copy(
                    isSpeechRecognitionAvailable = false,
                    isTtsFallbackActive = true,
                    isListening = false,
                    praiseMessage = "Speech recognizer is offline. Tap any word to read along with TTS!"
                )
                return@post
            }

            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _uiState.value = _uiState.value.copy(
                            isListening = true,
                            listeningForWord = targetWord,
                            praiseMessage = if (targetWord != null) "Listening for '$targetWord'..." else "Listening to you read..."
                        )
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _uiState.value = _uiState.value.copy(isListening = false)
                    }

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                            SpeechRecognizer.ERROR_NO_MATCH,
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Didn't catch that. Tap mic and read aloud again!"
                            else -> "Tap mic when you are ready to read."
                        }
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            listeningForWord = null,
                            praiseMessage = message
                        )
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

                try {
                    speechRecognizer?.cancel()
                } catch (_: Exception) {}

                val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
                val languageTag = script.primaryLocale.toLanguageTag()
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
                _uiState.value = _uiState.value.copy(
                    isListening = true,
                    listeningForWord = targetWord,
                    spokenTranscription = "",
                    praiseMessage = if (targetWord != null) "Say '$targetWord'..." else "Reading line... speak clearly"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    listeningForWord = null,
                    praiseMessage = "Tap mic to try reading aloud."
                )
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            _uiState.value = _uiState.value.copy(isListening = false, listeningForWord = null)
        }
    }

    private fun onSpeechRecognized(spokenText: String) {
        val targetWord = _uiState.value.listeningForWord
        val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
        val profileId = _uiState.value.document?.childProfileId ?: "default"

        _uiState.value = _uiState.value.copy(
            spokenTranscription = spokenText,
            isListening = false,
            listeningForWord = null
        )

        if (!targetWord.isNullOrBlank()) {
            val cleanTarget = UnicodeIndicTextUtil.cleanWordForMatching(targetWord)
            val isMatched = WordMatcher.isFuzzyMatch(spokenText, cleanTarget)
            if (isMatched) {
                _uiState.value = _uiState.value.copy(
                    matchedWords = _uiState.value.matchedWords + cleanTarget,
                    wordsToTryAgain = _uiState.value.wordsToTryAgain - cleanTarget,
                    isRepeatSuccessful = true,
                    praiseMessage = "🌟 Wonderful reading! You said '$targetWord' beautifully!"
                )
                viewModelScope.launch {
                    wordMasteryRepository.recordWordPractice(profileId, cleanTarget, script.name, wasSuccessful = true)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    wordsToTryAgain = _uiState.value.wordsToTryAgain + cleanTarget,
                    isRepeatSuccessful = false,
                    praiseMessage = "🌱 Nice try on '$targetWord'! Tap below to hear it slowly and try again."
                )
                viewModelScope.launch {
                    wordMasteryRepository.recordWordPractice(profileId, cleanTarget, script.name, wasSuccessful = false)
                }
            }
            return
        }

        val targetSentence = _uiState.value.parsedText?.sentences?.getOrNull(_uiState.value.currentSentenceIndex)
        if (targetSentence != null) {
            val sentenceWordList = targetSentence.words.map { it.cleanWord }
            val newlyMatched = WordMatcher.findMatchedWordsInSentence(spokenText, sentenceWordList)
            val updatedMatched = _uiState.value.matchedWords + newlyMatched

            // Collect unmatched words from this line into words to try again
            val unmatchedInSentence = sentenceWordList.filter { !updatedMatched.contains(it) }
            val updatedTryAgain = (_uiState.value.wordsToTryAgain + unmatchedInSentence) - newlyMatched

            val isWholeLineMatched = updatedMatched.containsAll(sentenceWordList)
            val warmMessage = if (isWholeLineMatched) {
                "🌟 Beautiful reading! You mastered this entire line!"
            } else if (newlyMatched.isNotEmpty()) {
                "🌟 Great job! You correctly read: ${newlyMatched.take(3).joinToString(", ")}!"
            } else {
                "🌱 Good effort! Tap any word below to hear how it sounds."
            }

            _uiState.value = _uiState.value.copy(
                matchedWords = updatedMatched,
                wordsToTryAgain = updatedTryAgain,
                praiseMessage = warmMessage
            )

            viewModelScope.launch {
                newlyMatched.forEach { word ->
                    wordMasteryRepository.recordWordPractice(profileId, word, script.name, wasSuccessful = true)
                }
                unmatchedInSentence.forEach { word ->
                    wordMasteryRepository.recordWordPractice(profileId, word, script.name, wasSuccessful = false)
                }
            }
        }
    }

    fun openRepeatModal(word: String) {
        val cleanWord = UnicodeIndicTextUtil.cleanWordForMatching(word)
        val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
        val syllables = PhonicsSyllableUtil.breakIntoSyllables(cleanWord, script.primaryLocale)
        _uiState.value = _uiState.value.copy(
            activeRepeatWord = cleanWord,
            isRepeatModalOpen = true,
            repeatSyllables = syllables,
            isRepeatSuccessful = false,
            praiseMessage = "🌟 Let's practice saying '$cleanWord'!"
        )
    }

    fun closeRepeatModal() {
        stopListening()
        _uiState.value = _uiState.value.copy(
            isRepeatModalOpen = false,
            activeRepeatWord = null,
            repeatSyllables = emptyList(),
            isRepeatSuccessful = false
        )
    }

    fun practiceSingleWord(word: String) {
        val cleanWord = UnicodeIndicTextUtil.cleanWordForMatching(word)
        val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
        val profileId = _uiState.value.document?.childProfileId ?: "default"

        viewModelScope.launch {
            wordMasteryRepository.recordWordPractice(profileId, cleanWord, script.name, wasSuccessful = true)
            _uiState.value = _uiState.value.copy(
                matchedWords = _uiState.value.matchedWords + cleanWord,
                wordsToTryAgain = _uiState.value.wordsToTryAgain - cleanWord,
                praiseMessage = "🎉 You mastered '$cleanWord'! Awesome job!"
            )
        }
    }

    fun speakWordSlowly(word: String) {
        val cleanWord = UnicodeIndicTextUtil.cleanWordForMatching(word)
        val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
        ttsManager.speakWordSlowlySyllables(cleanWord, script)
    }

    fun speakWordNormally(word: String) {
        val cleanWord = UnicodeIndicTextUtil.cleanWordForMatching(word)
        val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
        ttsManager.speakWord(cleanWord, script)
    }

    fun playSentenceTts() {
        val sentence = _uiState.value.parsedText?.sentences?.getOrNull(_uiState.value.currentSentenceIndex) ?: return
        val script = _uiState.value.parsedText?.script ?: SupportedScript.LATIN
        ttsManager.speakWord(sentence.sentence, script)
    }

    fun nextSentence() {
        val parsed = _uiState.value.parsedText ?: return
        val nextIdx = (_uiState.value.currentSentenceIndex + 1).coerceAtMost(parsed.sentences.size - 1)
        val sentence = parsed.sentences[nextIdx]
        _uiState.value = _uiState.value.copy(
            currentSentenceIndex = nextIdx,
            currentTargetWord = sentence.words.firstOrNull(),
            praiseMessage = "🌟 Keep going! You're doing wonderful!",
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
            praiseMessage = "🌟 Reading previous sentence.",
            spokenTranscription = ""
        )
    }

    override fun onCleared() {
        super.onCleared()
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
        }
    }
}
