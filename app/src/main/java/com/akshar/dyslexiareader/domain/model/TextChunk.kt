package com.akshar.dyslexiareader.domain.model

data class WordSpan(
    val word: String,
    val startIndex: Int,
    val endIndex: Int,
    val cleanWord: String,
    val syllables: List<String> = emptyList()
)

data class SentenceSpan(
    val sentence: String,
    val startIndex: Int,
    val endIndex: Int,
    val words: List<WordSpan>
)

data class FormattedReaderText(
    val fullText: String,
    val script: SupportedScript,
    val sentences: List<SentenceSpan>,
    val allWords: List<WordSpan>
)
