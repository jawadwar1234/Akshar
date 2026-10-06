package com.akshar.dyslexiareader.ui.reader

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.akshar.dyslexiareader.domain.model.SupportedScript
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class TtsPlaybackState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentSentenceIndex: Int = 0,
    val currentWordCharStart: Int = -1,
    val currentWordCharLength: Int = 0,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val isVoiceAvailable: Boolean = true
)

@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _playbackState = MutableStateFlow(TtsPlaybackState())
    val playbackState: StateFlow<TtsPlaybackState> = _playbackState.asStateFlow()

    private var sentenceList: List<String> = emptyList()
    private var sentenceCharOffsets: List<Int> = emptyList()
    private var currentScript: SupportedScript = SupportedScript.LATIN

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupUtteranceListener()
            applyLocale(currentScript.primaryLocale)
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _playbackState.value = _playbackState.value.copy(isPlaying = true, isPaused = false)
            }

            override fun onDone(utteranceId: String?) {
                val nextIndex = _playbackState.value.currentSentenceIndex + 1
                if (nextIndex < sentenceList.size) {
                    _playbackState.value = _playbackState.value.copy(currentSentenceIndex = nextIndex)
                    speakSentence(nextIndex)
                } else {
                    stop()
                }
            }

            override fun onError(utteranceId: String?) {
                _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = false)
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
                val sentenceIdx = _playbackState.value.currentSentenceIndex
                val offset = if (sentenceIdx < sentenceCharOffsets.size) sentenceCharOffsets[sentenceIdx] else 0
                _playbackState.value = _playbackState.value.copy(
                    currentWordCharStart = offset + start,
                    currentWordCharLength = end - start
                )
            }
        })
    }

    fun prepareText(sentences: List<String>, charOffsets: List<Int>, script: SupportedScript) {
        stop()
        this.sentenceList = sentences
        this.sentenceCharOffsets = charOffsets
        this.currentScript = script
        applyLocale(script.primaryLocale)
        _playbackState.value = _playbackState.value.copy(currentSentenceIndex = 0)
    }

    fun play() {
        if (!isInitialized || sentenceList.isEmpty()) return
        speakSentence(_playbackState.value.currentSentenceIndex)
    }

    fun pause() {
        tts?.stop()
        _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = true)
    }

    fun stop() {
        tts?.stop()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isPaused = false,
            currentSentenceIndex = 0,
            currentWordCharStart = -1,
            currentWordCharLength = 0
        )
    }

    fun nextSentence() {
        val next = (_playbackState.value.currentSentenceIndex + 1).coerceAtMost(sentenceList.size - 1)
        _playbackState.value = _playbackState.value.copy(currentSentenceIndex = next)
        if (_playbackState.value.isPlaying) {
            speakSentence(next)
        }
    }

    fun prevSentence() {
        val prev = (_playbackState.value.currentSentenceIndex - 1).coerceAtLeast(0)
        _playbackState.value = _playbackState.value.copy(currentSentenceIndex = prev)
        if (_playbackState.value.isPlaying) {
            speakSentence(prev)
        }
    }

    fun jumpToSentence(index: Int) {
        val clamped = index.coerceIn(0, sentenceList.size - 1)
        _playbackState.value = _playbackState.value.copy(currentSentenceIndex = clamped)
        if (_playbackState.value.isPlaying) {
            speakSentence(clamped)
        }
    }

    fun speakWord(word: String, script: SupportedScript = currentScript) {
        applyLocale(script.primaryLocale)
        tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "single_word_${System.currentTimeMillis()}")
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
        _playbackState.value = _playbackState.value.copy(speechRate = rate)
    }

    fun setSpeechPitch(pitch: Float) {
        tts?.setPitch(pitch)
        _playbackState.value = _playbackState.value.copy(speechPitch = pitch)
    }

    private fun speakSentence(index: Int) {
        if (index !in sentenceList.indices) return
        val sentence = sentenceList[index]
        tts?.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "utterance_$index")
    }

    private fun applyLocale(locale: Locale) {
        if (!isInitialized) return
        val result = tts?.setLanguage(locale)
        val isAvail = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        _playbackState.value = _playbackState.value.copy(isVoiceAvailable = isAvail)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
