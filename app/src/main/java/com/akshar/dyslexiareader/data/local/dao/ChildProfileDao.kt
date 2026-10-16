package com.akshar.dyslexiareader.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildProfileDao {

    @Query("SELECT * FROM child_profiles WHERE parentAccountId = :parentId ORDER BY createdAt ASC")
    fun getProfilesForParent(parentId: String): Flow<List<ChildProfileEntity>>

    @Query("SELECT * FROM child_profiles ORDER BY createdAt ASC")
    fun getAllProfiles(): Flow<List<ChildProfileEntity>>

    @Query("SELECT * FROM child_profiles ORDER BY createdAt ASC")
    suspend fun getAllProfilesDirect(): List<ChildProfileEntity>

    @Query("SELECT * FROM child_profiles WHERE parentAccountId = :parentId ORDER BY createdAt ASC")
    suspend fun getProfilesForParentDirect(parentId: String): List<ChildProfileEntity>

    @Query("SELECT * FROM child_profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: String): ChildProfileEntity?

    @Query("SELECT * FROM child_profiles WHERE id = :profileId LIMIT 1")
    fun observeProfileById(profileId: String): Flow<ChildProfileEntity?>

    @Query("SELECT * FROM child_profiles WHERE isSynced = 0")
    suspend fun getUnsyncedProfiles(): List<ChildProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ChildProfileEntity)

    @Update
    suspend fun updateProfile(profile: ChildProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ChildProfileEntity)

    @Query("DELETE FROM child_profiles WHERE id = :profileId")
    suspend fun deleteProfileById(profileId: String)

    @Query("DELETE FROM child_profiles")
    suspend fun clearAllProfiles()
}
