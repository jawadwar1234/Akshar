package com.akshar.dyslexiareader.domain.repository

import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import kotlinx.coroutines.flow.Flow

interface ChildProfileRepository {
    fun getProfilesForParent(parentId: String): Flow<List<ChildProfileEntity>>
    fun getAllProfiles(): Flow<List<ChildProfileEntity>>
    suspend fun getProfileById(profileId: String): ChildProfileEntity?
    fun observeProfileById(profileId: String): Flow<ChildProfileEntity?>
    suspend fun createProfile(profile: ChildProfileEntity)
    suspend fun updateProfile(profile: ChildProfileEntity)
    suspend fun deleteProfile(profileId: String)
    suspend fun deleteAllProfiles()
}
