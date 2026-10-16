package com.akshar.dyslexiareader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akshar.dyslexiareader.data.local.entity.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {

    @Query("SELECT * FROM reading_sessions WHERE childProfileId = :profileId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getSessionsForProfile(profileId: String): Flow<List<ReadingSessionEntity>>

    @Query("SELECT * FROM reading_sessions WHERE childProfileId = :profileId ORDER BY createdAt DESC")
    suspend fun getSessionsForProfileDirect(profileId: String): List<ReadingSessionEntity>

    @Query("SELECT * FROM reading_sessions")
    suspend fun getAllSessions(): List<ReadingSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordSession(session: ReadingSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ReadingSessionEntity>)

    @Query("DELETE FROM reading_sessions WHERE childProfileId = :profileId")
    suspend fun deleteSessionsForProfile(profileId: String)
}
