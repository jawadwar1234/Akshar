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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordSession(session: ReadingSessionEntity)
}
