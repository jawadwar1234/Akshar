package com.akshar.dyslexiareader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akshar.dyslexiareader.data.local.entity.ReaderSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReaderSettingsDao {

    @Query("SELECT * FROM reader_settings WHERE childProfileId = :profileId AND isDeleted = 0 LIMIT 1")
    fun getSettingsForProfile(profileId: String): Flow<ReaderSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ReaderSettingsEntity)

    @Update
    suspend fun updateSettings(settings: ReaderSettingsEntity)
}
