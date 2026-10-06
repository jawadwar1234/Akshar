package com.akshar.dyslexiareader.data.sync

import com.akshar.dyslexiareader.data.local.dao.ChildProfileDao
import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.dao.WordMasteryDao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val documentDao: DocumentDao,
    private val childProfileDao: ChildProfileDao,
    private val wordMasteryDao: WordMasteryDao
) {
    private val firestore: FirebaseFirestore by lazy {
        val db = FirebaseFirestore.getInstance()
        // Enable offline persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        db
    }

    suspend fun syncPendingData(parentAccountId: String) = withContext(Dispatchers.IO) {
        if (parentAccountId.startsWith("offline")) return@withContext

        try {
            // 1. Sync unsynced child profiles
            val unsyncedProfiles = childProfileDao.getUnsyncedProfiles()
            for (profile in unsyncedProfiles) {
                val profileMap = hashMapOf(
                    "id" to profile.id,
                    "displayName" to profile.displayName,
                    "gradeLevel" to profile.gradeLevel,
                    "pinType" to profile.pinType,
                    "hashedPin" to profile.hashedPin,
                    "salt" to profile.salt,
                    "updatedAt" to profile.updatedAt
                )
                firestore.collection("users")
                    .document(parentAccountId)
                    .collection("profiles")
                    .document(profile.id)
                    .set(profileMap)

                childProfileDao.updateProfile(profile.copy(isSynced = true))
            }

            // 2. Sync unsynced documents
            val unsyncedDocs = documentDao.getUnsyncedDocuments()
            for (doc in unsyncedDocs) {
                val docMap = hashMapOf(
                    "id" to doc.id,
                    "title" to doc.title,
                    "cleanText" to doc.cleanText,
                    "detectedScript" to doc.detectedScript,
                    "wordCount" to doc.wordCount,
                    "readingProgressPercent" to doc.readingProgressPercent,
                    "lastReadWordIndex" to doc.lastReadWordIndex,
                    "updatedAt" to doc.updatedAt
                )
                firestore.collection("users")
                    .document(parentAccountId)
                    .collection("profiles")
                    .document(doc.childProfileId)
                    .collection("documents")
                    .document(doc.id)
                    .set(docMap)

                documentDao.updateDocument(doc.copy(isSynced = true))
            }
        } catch (e: Exception) {
            // Gracefully ignore network errors — Room remains the source of truth!
        }
    }
}
