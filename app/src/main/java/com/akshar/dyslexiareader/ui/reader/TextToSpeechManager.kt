package com.akshar.dyslexiareader.ui.reader

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.akshar.dyslexiareader.domain.model.SupportedScript
import com.akshar.dyslexiareader.domain.util.PhonicsSyllableUtil
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
    val isVoiceAvailable: Boolean = true,
    val isRangeStartSupported: Boolean = true,
    val missingLanguageName: String? = null
)

@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _playbackState = MutableStateFlow(TtsPlaybackState())
    val playbackState: StateFlow<TtsPlaybackState> = _playbackState.asStateFlow()

    private var sentenceList: List<String> = emptyList()
    private var sentenceCharOffsets: List<Int> = emptyList()
    private var currentScript: SupportedScript = SupportedScript.LATIN
    private var isSlowSyllableSpeaking = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupUtteranceListener()
            checkAndApplyLanguage(currentScript.primaryLocale)
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _playbackState.value = _playbackState.value.copy(isPlaying = true, isPaused = false)
            }

            override fun onDone(utteranceId: String?) {
                if (isSlowSyllableSpeaking) {
                    isSlowSyllableSpeaking = false
                    return
                }
                val nextIndex = _playbackState.value.currentSentenceIndex + 1
                if (nextIndex < sentenceList.size) {
                    _playbackState.value = _playbackState.value.copy(
                        currentSentenceIndex = nextIndex,
                        currentWordCharStart = if (nextIndex < sentenceCharOffsets.size) sentenceCharOffsets[nextIndex] else -1,
                        currentWordCharLength = 0
                    )
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
                    isRangeStartSupported = true,
                    currentWordCharStart = offset + start,
                    currentWordCharLength = end - start
                )
            }
        })
    }

    fun prepareText(sentences: List<String>, charOffsets: List<Int>, script: SupportedScript) {
        stop()
        this.sentenceList = chunkLongSentences(sentences)
        this.sentenceCharOffsets = charOffsets
        this.currentScript = script
        checkAndApplyLanguage(script.primaryLocale)
        _playbackState.value = _playbackState.value.copy(currentSentenceIndex = 0)
    }

    /**
     * Chunks text into utterances under the engine's max speech input length.
     */
    private fun chunkLongSentences(sentences: List<String>): List<String> {
        val maxLength = 3900 // Well under Android TTS limit of 4000 chars
        val chunked = mutableListOf<String>()
        for (s in sentences) {
            if (s.length <= maxLength) {
                chunked.add(s)
            } else {
                var start = 0
                while (start < s.length) {
                    val end = (start + maxLength).coerceAtMost(s.length)
                    chunked.add(s.substring(start, end))
                    start = end
                }
            }
        }
        return chunked
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

    /**
     * Tap to hear word at normal reading rate.
     */
    fun speakWord(word: String, script: SupportedScript = currentScript) {
        checkAndApplyLanguage(script.primaryLocale)
        tts?.setSpeechRate(_playbackState.value.speechRate)
        tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "single_word_${System.currentTimeMillis()}")
    }

    /**
     * Long-press to hear word slowly, syllable by syllable.
     */
    fun speakWordSlowlySyllables(word: String, script: SupportedScript = currentScript) {
        checkAndApplyLanguage(script.primaryLocale)
        val syllables = PhonicsSyllableUtil.breakIntoSyllables(word, script.primaryLocale)
        if (syllables.isEmpty()) return

        isSlowSyllableSpeaking = true
        tts?.stop()
        tts?.setSpeechRate(0.6f) // Slower pace for sounding out phonics

        syllables.forEachIndexed { index, syllable ->
            mainHandler.postDelayed({
                val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                tts?.speak(syllable, queueMode, null, "syllable_${index}_${System.currentTimeMillis()}")
                // Return speech rate back after final syllable
                if (index == syllables.size - 1) {
                    mainHandler.postDelayed({
                        tts?.setSpeechRate(_playbackState.value.speechRate)
                    }, 500)
                }
            }, (index * 600L))
        }
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

    fun checkAndApplyLanguage(locale: Locale) {
        if (!isInitialized) return
        val avail = tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        val isSupported = avail >= TextToSpeech.LANG_AVAILABLE

        if (isSupported) {
            tts?.setLanguage(locale)
            _playbackState.value = _playbackState.value.copy(
                isVoiceAvailable = true,
                missingLanguageName = null
            )
        } else {
            _playbackState.value = _playbackState.value.copy(
                isVoiceAvailable = false,
                missingLanguageName = locale.displayLanguage
            )
        }
    }

    /**
     * Opens system Text-To-Speech settings to allow user to install voice packs.
     */
    fun openTtsSettings() {
        try {
            val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                // Ignore gracefully
            }
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
