package com.akshar.dyslexiareader.domain.repository

import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity
import kotlinx.coroutines.flow.Flow

interface WordMasteryRepository {
    fun getMasteredWords(profileId: String): Flow<List<WordMasteryEntity>>
    fun getAllWords(profileId: String): Flow<List<WordMasteryEntity>>
    fun getMasteredCount(profileId: String): Flow<Int>
    suspend fun recordWordRead(profileId: String, word: String, script: String)
    suspend fun recordWordPractice(profileId: String, word: String, script: String, wasSuccessful: Boolean)
    suspend fun deleteWord(wordId: String)
    suspend fun clearMasteryForProfile(profileId: String)
}
