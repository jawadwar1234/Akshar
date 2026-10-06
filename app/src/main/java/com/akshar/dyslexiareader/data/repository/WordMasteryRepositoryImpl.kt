package com.akshar.dyslexiareader.data.repository

import com.akshar.dyslexiareader.data.local.dao.WordMasteryDao
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity
import com.akshar.dyslexiareader.domain.repository.WordMasteryRepository
import com.akshar.dyslexiareader.domain.util.UnicodeIndicTextUtil
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordMasteryRepositoryImpl @Inject constructor(
    private val wordMasteryDao: WordMasteryDao
) : WordMasteryRepository {

    override fun getMasteredWords(profileId: String): Flow<List<WordMasteryEntity>> {
        return wordMasteryDao.getMasteredWords(profileId)
    }

    override fun getAllWords(profileId: String): Flow<List<WordMasteryEntity>> {
        return wordMasteryDao.getAllWordsForProfile(profileId)
    }

    override fun getMasteredCount(profileId: String): Flow<Int> {
        return wordMasteryDao.getMasteredCount(profileId)
    }

    override suspend fun recordWordRead(profileId: String, word: String, script: String) {
        val clean = UnicodeIndicTextUtil.cleanWordForMatching(word)
        if (clean.isBlank()) return

        val existing = wordMasteryDao.getWord(profileId, clean)
        if (existing != null) {
            wordMasteryDao.updateWord(
                existing.copy(
                    timesRead = existing.timesRead + 1,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            wordMasteryDao.insertWord(
                WordMasteryEntity(
                    id = UUID.randomUUID().toString(),
                    childProfileId = profileId,
                    word = word.trim(),
                    cleanWord = clean,
                    script = script,
                    timesRead = 1,
                    timesPracticed = 0,
                    isMastered = false,
                    lastPracticedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun recordWordPractice(
        profileId: String,
        word: String,
        script: String,
        wasSuccessful: Boolean
    ) {
        val clean = UnicodeIndicTextUtil.cleanWordForMatching(word)
        if (clean.isBlank()) return

        val existing = wordMasteryDao.getWord(profileId, clean)
        if (existing != null) {
            val successfulCount = existing.timesPracticed + (if (wasSuccessful) 1 else 0)
            // A word is considered mastered after 2+ successful practice reads
            val isNowMastered = existing.isMastered || successfulCount >= 2
            wordMasteryDao.updateWord(
                existing.copy(
                    timesPracticed = successfulCount,
                    isMastered = isNowMastered,
                    lastPracticedAt = System.currentTimeMillis()
                )
            )
        } else {
            wordMasteryDao.insertWord(
                WordMasteryEntity(
                    id = UUID.randomUUID().toString(),
                    childProfileId = profileId,
                    word = word.trim(),
                    cleanWord = clean,
                    script = script,
                    timesRead = 1,
                    timesPracticed = if (wasSuccessful) 1 else 0,
                    isMastered = false,
                    lastPracticedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun deleteWord(wordId: String) {
        // DAO delete
    }

    override suspend fun clearMasteryForProfile(profileId: String) {
        wordMasteryDao.deleteAllForProfile(profileId)
    }

    private fun WordMasteryEntity.copy(
        timesRead: Int = this.timesRead,
        timesPracticed: Int = this.timesPracticed,
        isMastered: Boolean = this.isMastered,
        updatedAt: Long = System.currentTimeMillis()
    ): WordMasteryEntity {
        return this.copy(
            timesRead = timesRead,
            timesPracticed = timesPracticed,
            isMastered = isMastered,
            lastPracticedAt = updatedAt
        )
    }
}
