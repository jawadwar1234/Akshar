package com.akshar.dyslexiareader.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordMasteryDao {

    @Query("SELECT * FROM word_mastery WHERE childProfileId = :profileId AND isMastered = 1 ORDER BY lastPracticedAt DESC")
    fun getMasteredWords(profileId: String): Flow<List<WordMasteryEntity>>

    @Query("SELECT * FROM word_mastery WHERE childProfileId = :profileId AND isMastered = 1 ORDER BY lastPracticedAt DESC")
    suspend fun getMasteredWordsDirect(profileId: String): List<WordMasteryEntity>

    @Query("SELECT * FROM word_mastery WHERE childProfileId = :profileId ORDER BY lastPracticedAt DESC")
    fun getAllWordsForProfile(profileId: String): Flow<List<WordMasteryEntity>>

    @Query("SELECT * FROM word_mastery WHERE childProfileId = :profileId ORDER BY lastPracticedAt DESC")
    suspend fun getAllWordsForProfileDirect(profileId: String): List<WordMasteryEntity>

    @Query("SELECT * FROM word_mastery")
    suspend fun getAllWordsDirect(): List<WordMasteryEntity>

    @Query("SELECT * FROM word_mastery WHERE childProfileId = :profileId AND cleanWord = :cleanWord LIMIT 1")
    suspend fun getWord(profileId: String, cleanWord: String): WordMasteryEntity?

    @Query("SELECT COUNT(*) FROM word_mastery WHERE childProfileId = :profileId AND isMastered = 1")
    fun getMasteredCount(profileId: String): Flow<Int>

    @Query("SELECT * FROM word_mastery WHERE isSynced = 0")
    suspend fun getUnsyncedWords(): List<WordMasteryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordMasteryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordMasteryEntity>)

    @Update
    suspend fun updateWord(word: WordMasteryEntity)

    @Delete
    suspend fun deleteWord(word: WordMasteryEntity)

    @Query("DELETE FROM word_mastery WHERE childProfileId = :profileId")
    suspend fun deleteAllForProfile(profileId: String)

    @Query("DELETE FROM word_mastery WHERE childProfileId = :profileId")
    suspend fun deleteWordsForProfile(profileId: String)

    @Query("DELETE FROM word_mastery")
    suspend fun clearAllWords()
}
