package com.akshar.dyslexiareader.data.repository

import com.akshar.dyslexiareader.data.local.dao.ChildProfileDao
import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.dao.WordMasteryDao
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.domain.repository.ChildProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildProfileRepositoryImpl @Inject constructor(
    private val childProfileDao: ChildProfileDao,
    private val documentDao: DocumentDao,
    private val wordMasteryDao: WordMasteryDao
) : ChildProfileRepository {

    override fun getProfilesForParent(parentId: String): Flow<List<ChildProfileEntity>> {
        return childProfileDao.getProfilesForParent(parentId)
    }

    override fun getAllProfiles(): Flow<List<ChildProfileEntity>> {
        return childProfileDao.getAllProfiles()
    }

    override suspend fun getProfileById(profileId: String): ChildProfileEntity? {
        return childProfileDao.getProfileById(profileId)
    }

    override fun observeProfileById(profileId: String): Flow<ChildProfileEntity?> {
        return childProfileDao.observeProfileById(profileId)
    }

    override suspend fun createProfile(profile: ChildProfileEntity) {
        childProfileDao.insertProfile(profile)
    }

    override suspend fun updateProfile(profile: ChildProfileEntity) {
        childProfileDao.updateProfile(profile)
    }

    override suspend fun deleteProfile(profileId: String) {
        // Cascade delete all documents and word mastery for this profile
        documentDao.deleteAllForProfile(profileId)
        wordMasteryDao.deleteAllForProfile(profileId)
        childProfileDao.deleteProfileById(profileId)
    }

    override suspend fun deleteAllProfiles() {
        documentDao.clearAllDocuments()
        wordMasteryDao.clearAllWords()
        childProfileDao.clearAllProfiles()
    }
}
