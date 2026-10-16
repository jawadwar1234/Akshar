package com.akshar.dyslexiareader.util.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.akshar.dyslexiareader.util.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceCommandManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null
    private var isInitialized = false
    private var currentLanguageCode: String = "en-IN"
    private var isHandsFreeActive: Boolean = false

    private val _state = MutableStateFlow(VoiceRecognitionState())
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()

    private val _commandEvents = MutableSharedFlow<VoiceCommand>(extraBufferCapacity = 1)
    val commandEvents: SharedFlow<VoiceCommand> = _commandEvents.asSharedFlow()

    fun isSpeechRecognitionAvailable(): Boolean {
        return try {
            SpeechRecognizer.isRecognitionAvailable(context)
        } catch (_: Exception) {
            false
        }
    }

    private fun ensureRecognizer(): Boolean {
        if (!isSpeechRecognitionAvailable()) {
            return false
        }
        if (speechRecognizer == null) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }
                isInitialized = true
                return true
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isListening = false,
                    feedbackMessage = "Speech recognition unavailable"
                )
                return false
            }
        }
        return true
    }

    fun startListening(
        language: AppLanguage = AppLanguage.ENGLISH,
        handsFree: Boolean = false
    ) {
        mainHandler.post {
            isHandsFreeActive = handsFree
            currentLanguageCode = mapLanguageToSpeechCode(language)

            val isAvailable = ensureRecognizer()
            if (!isAvailable || speechRecognizer == null) {
                _state.value = _state.value.copy(
                    isListening = false,
                    isLanguagePackMissing = false,
                    feedbackMessage = "Voice recognition service is not available on this device."
                )
                return@post
            }

            try {
                // Cancel any ongoing recognition before starting a new one
                try {
                    speechRecognizer?.cancel()
                } catch (_: Exception) {}

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageCode)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLanguageCode)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
                _state.value = _state.value.copy(
                    isListening = true,
                    isHandsFreeMode = handsFree,
                    partialText = "",
                    feedbackMessage = "Listening for commands..."
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isListening = false,
                    feedbackMessage = "Unable to start listening."
                )
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            isHandsFreeActive = false
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            _state.value = _state.value.copy(
                isListening = false,
                isHandsFreeMode = false,
                currentAmplitude = 0f
            )
        }
    }

    fun toggleHandsFree(language: AppLanguage = AppLanguage.ENGLISH) {
        if (_state.value.isHandsFreeMode && _state.value.isListening) {
            stopListening()
        } else {
            startListening(language = language, handsFree = true)
        }
    }

    fun dismissMissingLanguageDialog() {
        _state.value = _state.value.copy(isLanguagePackMissing = false)
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.value = _state.value.copy(
                    isListening = true,
                    feedbackMessage = "Listening..."
                )
            }

            override fun onBeginningOfSpeech() {
                _state.value = _state.value.copy(feedbackMessage = "Hearing speech...")
            }

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.0f, 1.0f)
                _state.value = _state.value.copy(currentAmplitude = normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _state.value = _state.value.copy(
                    currentAmplitude = 0f,
                    feedbackMessage = "Processing command..."
                )
            }

            override fun onError(error: Int) {
                _state.value = _state.value.copy(currentAmplitude = 0f)

                when (error) {
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        _state.value = _state.value.copy(
                            isListening = false,
                            feedbackMessage = "Microphone permission is required."
                        )
                    }
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        if (isHandsFreeActive) {
                            mainHandler.postDelayed({
                                if (isHandsFreeActive) {
                                    startListening(handsFree = true)
                                }
                            }, 400L)
                        } else {
                            _state.value = _state.value.copy(
                                isListening = false,
                                feedbackMessage = "Didn't catch that. Tap mic to try again."
                            )
                        }
                    }
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        try {
                            speechRecognizer?.cancel()
                        } catch (_: Exception) {}
                        if (isHandsFreeActive) {
                            mainHandler.postDelayed({
                                if (isHandsFreeActive) {
                                    startListening(handsFree = true)
                                }
                            }, 500L)
                        } else {
                            _state.value = _state.value.copy(isListening = false)
                        }
                    }
                    else -> {
                        if (isHandsFreeActive) {
                            mainHandler.postDelayed({
                                if (isHandsFreeActive) {
                                    startListening(handsFree = true)
                                }
                            }, 500L)
                        } else {
                            _state.value = _state.value.copy(
                                isListening = false,
                                feedbackMessage = "Tap mic to speak command."
                            )
                        }
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: ""

                if (spokenText.isNotBlank()) {
                    val command = VoiceCommandMatcher.matchCommand(spokenText)

                    _state.value = _state.value.copy(
                        lastRecognizedText = spokenText,
                        lastCommand = command,
                        feedbackMessage = if (command != VoiceCommand.UNKNOWN) "Command: ${command.defaultDescription}" else "\"$spokenText\" not recognized"
                    )

                    if (command != VoiceCommand.UNKNOWN) {
                        scope.launch {
                            _commandEvents.emit(command)
                        }
                    }
                }

                if (isHandsFreeActive) {
                    mainHandler.postDelayed({
                        if (isHandsFreeActive) {
                            startListening(handsFree = true)
                        }
                    }, 600L)
                } else {
                    _state.value = _state.value.copy(isListening = false)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull() ?: ""
                if (partial.isNotBlank()) {
                    _state.value = _state.value.copy(
                        partialText = partial,
                        feedbackMessage = "\"$partial\""
                    )
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun mapLanguageToSpeechCode(language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> "en-IN"
            AppLanguage.HINDI -> "hi-IN"
            AppLanguage.MARATHI -> "mr-IN"
            AppLanguage.GUJARATI -> "gu-IN"
            AppLanguage.BENGALI -> "bn-IN"
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
        }
    }
}
