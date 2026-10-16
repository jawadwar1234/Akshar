package com.akshar.dyslexiareader.util.voice

data class VoiceRecognitionState(
    val isListening: Boolean = false,
    val isHandsFreeMode: Boolean = false,
    val currentAmplitude: Float = 0.0f, // 0.0f to 1.0f for waveform pulsing
    val partialText: String = "",
    val lastRecognizedText: String? = null,
    val lastCommand: VoiceCommand? = null,
    val feedbackMessage: String? = null,
    val isLanguagePackMissing: Boolean = false,
    val missingLanguageName: String? = null,
    val isPermissionGranted: Boolean = false
)
