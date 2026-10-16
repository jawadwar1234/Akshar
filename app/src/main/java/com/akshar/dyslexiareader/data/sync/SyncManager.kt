package com.akshar.dyslexiareader.data.sync

import android.net.Uri
import com.akshar.dyslexiareader.data.local.dao.ChildProfileDao
import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.dao.PageDao
import com.akshar.dyslexiareader.data.local.dao.ReaderSettingsDao
import com.akshar.dyslexiareader.data.local.dao.ReadingSessionDao
import com.akshar.dyslexiareader.data.local.dao.WordMasteryDao
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.local.entity.ReaderSettingsEntity
import com.akshar.dyslexiareader.data.local.entity.ReadingSessionEntity
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity
import com.akshar.dyslexiareader.data.repository.UserPreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val documentDao: DocumentDao,
    private val childProfileDao: ChildProfileDao,
    private val wordMasteryDao: WordMasteryDao,
    private val pageDao: PageDao,
    private val readerSettingsDao: ReaderSettingsDao,
    private val readingSessionDao: ReadingSessionDao,
    private val userPreferencesRepository: UserPreferencesRepository? = null
) {
    private val _syncStatus = MutableStateFlow(SyncStatus.SAVED_LOCAL)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val firestore: FirebaseFirestore by lazy {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        db
    }

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    suspend fun syncPendingData(parentAccountId: String) = withContext(Dispatchers.IO) {
        if (parentAccountId.isBlank() || parentAccountId.startsWith("offline")) {
            _syncStatus.value = SyncStatus.SAVED_LOCAL
            return@withContext
        }

        try {
            _syncStatus.value = SyncStatus.SYNCING

            // --- 1. PUSH LOCAL CHANGES TO FIRESTORE ---
            pushLocalProfiles(parentAccountId)
            pushLocalDocuments(parentAccountId)
            pushLocalReaderSettings(parentAccountId)
            pushLocalReadingSessions(parentAccountId)
            pushLocalPracticeWords(parentAccountId)

            // --- 2. PULL REMOTE CHANGES FROM FIRESTORE ---
            pullRemoteProfiles(parentAccountId)
            pullRemoteDataForProfiles(parentAccountId)

            _syncStatus.value = SyncStatus.SAVED_TO_CLOUD
        } catch (e: Exception) {
            // Graceful fallback — Room database stays the source of truth
            _syncStatus.value = SyncStatus.SAVED_LOCAL
        }
    }

    // ==========================================
    // PUSH LOGIC
    // ==========================================

    private suspend fun pushLocalProfiles(parentAccountId: String) {
        val unsyncedProfiles = childProfileDao.getUnsyncedProfiles()
        for (profile in unsyncedProfiles) {
            val profileMap = hashMapOf(
                "id" to profile.id,
                "parentAccountId" to profile.parentAccountId,
                "displayName" to profile.displayName,
                "avatarIconName" to profile.avatarIconName,
                "pinType" to profile.pinType,
                "hashedPin" to profile.hashedPin,
                "salt" to profile.salt,
                "gradeLevel" to profile.gradeLevel,
                "preferredThemeId" to profile.preferredThemeId,
                "preferredFontId" to profile.preferredFontId,
                "preferredFontSizeSp" to profile.preferredFontSizeSp.toDouble(),
                "preferredLineSpacing" to profile.preferredLineSpacing.toDouble(),
                "preferredLetterSpacing" to profile.preferredLetterSpacing.toDouble(),
                "preferredTtsSpeed" to profile.preferredTtsSpeed.toDouble(),
                "preferredTtsPitch" to profile.preferredTtsPitch.toDouble(),
                "createdAt" to profile.createdAt,
                "updatedAt" to profile.updatedAt,
                "isDeleted" to profile.isDeleted
            )

            firestore.collection("users")
                .document(parentAccountId)
                .collection("profiles")
                .document(profile.id)
                .set(profileMap, SetOptions.merge())
                .await()

            childProfileDao.updateProfile(profile.copy(isSynced = true))
        }
    }

    private suspend fun pushLocalDocuments(parentAccountId: String) {
        val unsyncedDocs = documentDao.getUnsyncedDocuments()
        val userSettings = userPreferencesRepository?.userSettingsFlow?.first()
        val backupPhotos = userSettings?.backupPhotosToCloud ?: false

        for (doc in unsyncedDocs) {
            val docMap = hashMapOf(
                "id" to doc.id,
                "childProfileId" to doc.childProfileId,
                "title" to doc.title,
                "cleanText" to doc.cleanText,
                "rawOcrText" to doc.rawOcrText,
                "detectedScript" to doc.detectedScript,
                "subject" to doc.subject,
                "isFavorite" to doc.isFavorite,
                "tags" to doc.tags,
                "wordCount" to doc.wordCount,
                "readingProgressPercent" to doc.readingProgressPercent,
                "lastReadWordIndex" to doc.lastReadWordIndex,
                "createdAt" to doc.createdAt,
                "updatedAt" to doc.updatedAt,
                "isDeleted" to doc.isDeleted
            )

            firestore.collection("users")
                .document(parentAccountId)
                .collection("profiles")
                .document(doc.childProfileId)
                .collection("documents")
                .document(doc.id)
                .set(docMap, SetOptions.merge())
                .await()

            // Optional photo backup to Firebase Storage if opt-in enabled
            if (backupPhotos && !doc.imagePath.isNullOrBlank()) {
                val imageFile = File(doc.imagePath)
                if (imageFile.exists()) {
                    try {
                        val storageRef = storage.reference
                            .child("users/$parentAccountId/profiles/${doc.childProfileId}/documents/${doc.id}/page_photo.jpg")
                        storageRef.putFile(Uri.fromFile(imageFile)).await()
                    } catch (_: Exception) {
                        // Ignore photo upload failures gracefully
                    }
                }
            }

            documentDao.updateDocument(doc.copy(isSynced = true))
        }
    }

    private suspend fun pushLocalReaderSettings(parentAccountId: String) {
        val allSettings = readerSettingsDao.getAllSettings()
        for (setting in allSettings) {
            val settingMap = hashMapOf(
                "id" to setting.id,
                "childProfileId" to setting.childProfileId,
                "theme" to setting.theme,
                "fontSize" to setting.fontSize.toDouble(),
                "lineSpacing" to setting.lineSpacing.toDouble(),
                "letterSpacing" to setting.letterSpacing.toDouble(),
                "wordSpacing" to setting.wordSpacing.toDouble(),
                "marginWidth" to setting.marginWidth,
                "fontFamily" to setting.fontFamily,
                "rulerEnabled" to setting.rulerEnabled,
                "rulerWindowLines" to setting.rulerWindowLines,
                "bionicEnabled" to setting.bionicEnabled,
                "syllablesEnabled" to setting.syllablesEnabled,
                "chunkingPaletteIndex" to setting.chunkingPaletteIndex,
                "ttsSpeed" to setting.ttsSpeed.toDouble(),
                "ttsPitch" to setting.ttsPitch.toDouble(),
                "createdAt" to setting.createdAt,
                "updatedAt" to setting.updatedAt,
                "isDeleted" to setting.isDeleted
            )

            firestore.collection("users")
                .document(parentAccountId)
                .collection("profiles")
                .document(setting.childProfileId)
                .collection("settings")
                .document("reader_settings")
                .set(settingMap, SetOptions.merge())
                .await()
        }
    }

    private suspend fun pushLocalReadingSessions(parentAccountId: String) {
        val sessions = readingSessionDao.getAllSessions()
        for (session in sessions) {
            val sessionMap = hashMapOf(
                "id" to session.id,
                "childProfileId" to session.childProfileId,
                "documentId" to session.documentId,
                "durationSeconds" to session.durationSeconds,
                "wordsReadCount" to session.wordsReadCount,
                "startProgressPercent" to session.startProgressPercent,
                "endProgressPercent" to session.endProgressPercent,
                "lastReadWordIndex" to session.lastReadWordIndex,
                "createdAt" to session.createdAt,
                "updatedAt" to session.updatedAt,
                "isDeleted" to session.isDeleted
            )

            firestore.collection("users")
                .document(parentAccountId)
                .collection("profiles")
                .document(session.childProfileId)
                .collection("sessions")
                .document(session.id)
                .set(sessionMap, SetOptions.merge())
                .await()
        }
    }

    private suspend fun pushLocalPracticeWords(parentAccountId: String) {
        val unsyncedWords = wordMasteryDao.getUnsyncedWords()
        for (word in unsyncedWords) {
            val wordMap = hashMapOf(
                "id" to word.id,
                "childProfileId" to word.childProfileId,
                "word" to word.word,
                "cleanWord" to word.cleanWord,
                "script" to word.script,
                "timesRead" to word.timesRead,
                "timesPracticed" to word.timesPracticed,
                "isMastered" to word.isMastered,
                "lastPracticedAt" to word.lastPracticedAt,
                "createdAt" to word.createdAt,
                "updatedAt" to word.updatedAt,
                "isDeleted" to word.isDeleted
            )

            firestore.collection("users")
                .document(parentAccountId)
                .collection("profiles")
                .document(word.childProfileId)
                .collection("practiceWords")
                .document(word.id)
                .set(wordMap, SetOptions.merge())
                .await()

            wordMasteryDao.updateWord(word.copy(isSynced = true))
        }
    }

    // ==========================================
    // PULL & CONFLICT RESOLUTION LOGIC
    // ==========================================

    private suspend fun pullRemoteProfiles(parentAccountId: String) {
        val profilesSnapshot = firestore.collection("users")
            .document(parentAccountId)
            .collection("profiles")
            .get()
            .await()

        for (doc in profilesSnapshot.documents) {
            val profileId = doc.getString("id") ?: doc.id
            val remoteProfile = ChildProfileEntity(
                id = profileId,
                parentAccountId = parentAccountId,
                displayName = doc.getString("displayName") ?: "Student",
                avatarIconName = doc.getString("avatarIconName") ?: "avatar_1",
                pinType = doc.getString("pinType") ?: "PICTURE",
                hashedPin = doc.getString("hashedPin") ?: "",
                salt = doc.getString("salt") ?: "",
                gradeLevel = doc.getString("gradeLevel") ?: "Grade 3",
                preferredThemeId = doc.getString("preferredThemeId") ?: "cream",
                preferredFontId = doc.getString("preferredFontId") ?: "lexend",
                preferredFontSizeSp = (doc.getDouble("preferredFontSizeSp") ?: 22.0).toFloat(),
                preferredLineSpacing = (doc.getDouble("preferredLineSpacing") ?: 1.6).toFloat(),
                preferredLetterSpacing = (doc.getDouble("preferredLetterSpacing") ?: 0.08).toFloat(),
                preferredTtsSpeed = (doc.getDouble("preferredTtsSpeed") ?: 1.0).toFloat(),
                preferredTtsPitch = (doc.getDouble("preferredTtsPitch") ?: 1.0).toFloat(),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                isSynced = true,
                isDeleted = doc.getBoolean("isDeleted") ?: false
            )

            val localProfile = childProfileDao.getProfileById(profileId)
            if (localProfile != null) {
                val resolved = SyncConflictResolver.resolveProfileConflict(localProfile, remoteProfile)
                childProfileDao.updateProfile(resolved)
            } else {
                childProfileDao.insertProfile(remoteProfile)
            }
        }
    }

    private suspend fun pullRemoteDataForProfiles(parentAccountId: String) {
        val profiles = childProfileDao.getProfilesForParentDirect(parentAccountId)
        for (profile in profiles) {
            // 1. Documents
            val docsSnapshot = firestore.collection("users")
                .document(parentAccountId)
                .collection("profiles")
                .document(profile.id)
                .collection("documents")
                .get()
                .await()

            for (d in docsSnapshot.documents) {
                val docId = d.getString("id") ?: d.id
                val remoteDoc = DocumentEntity(
                    id = docId,
                    childProfileId = profile.id,
                    title = d.getString("title") ?: "Untitled",
                    rawOcrText = d.getString("rawOcrText") ?: "",
                    cleanText = d.getString("cleanText") ?: "",
                    detectedScript = d.getString("detectedScript") ?: "LATIN",
                    imagePath = null, // Cloud synced without forcing large local files
                    subject = d.getString("subject") ?: "General",
                    isFavorite = d.getBoolean("isFavorite") ?: false,
                    tags = d.getString("tags") ?: "",
                    wordCount = (d.getLong("wordCount") ?: 0L).toInt(),
                    readingProgressPercent = (d.getLong("readingProgressPercent") ?: 0L).toInt(),
                    lastReadWordIndex = (d.getLong("lastReadWordIndex") ?: 0L).toInt(),
                    createdAt = d.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = d.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isSynced = true,
                    isDeleted = d.getBoolean("isDeleted") ?: false
                )

                val localDoc = documentDao.getDocumentByIdDirect(docId)
                if (localDoc != null) {
                    val resolved = SyncConflictResolver.resolveDocumentConflict(localDoc, remoteDoc)
                    documentDao.updateDocument(resolved)
                } else {
                    documentDao.insertDocument(remoteDoc)
                }
            }

            // 2. Reader Settings
            val settingsDoc = firestore.collection("users")
                .document(parentAccountId)
                .collection("profiles")
                .document(profile.id)
                .collection("settings")
                .document("reader_settings")
                .get()
                .await()

            if (settingsDoc.exists()) {
                val remoteSettings = ReaderSettingsEntity(
                    id = settingsDoc.getString("id") ?: "setting_${profile.id}",
                    childProfileId = profile.id,
                    theme = settingsDoc.getString("theme") ?: "cream",
                    fontSize = (settingsDoc.getDouble("fontSize") ?: 22.0).toFloat(),
                    lineSpacing = (settingsDoc.getDouble("lineSpacing") ?: 1.6).toFloat(),
                    letterSpacing = (settingsDoc.getDouble("letterSpacing") ?: 0.08).toFloat(),
                    wordSpacing = (settingsDoc.getDouble("wordSpacing") ?: 0.1).toFloat(),
                    marginWidth = (settingsDoc.getLong("marginWidth") ?: 16L).toInt(),
                    fontFamily = settingsDoc.getString("fontFamily") ?: "lexend",
                    rulerEnabled = settingsDoc.getBoolean("rulerEnabled") ?: false,
                    rulerWindowLines = (settingsDoc.getLong("rulerWindowLines") ?: 1L).toInt(),
                    bionicEnabled = settingsDoc.getBoolean("bionicEnabled") ?: false,
                    syllablesEnabled = settingsDoc.getBoolean("syllablesEnabled") ?: false,
                    chunkingPaletteIndex = (settingsDoc.getLong("chunkingPaletteIndex") ?: 0L).toInt(),
                    ttsSpeed = (settingsDoc.getDouble("ttsSpeed") ?: 1.0).toFloat(),
                    ttsPitch = (settingsDoc.getDouble("ttsPitch") ?: 1.0).toFloat(),
                    createdAt = settingsDoc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = settingsDoc.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isDeleted = settingsDoc.getBoolean("isDeleted") ?: false
                )

                val localSettings = readerSettingsDao.getSettingsForProfileDirect(profile.id)
                if (localSettings != null) {
                    val resolved = SyncConflictResolver.resolveSettingsConflict(localSettings, remoteSettings)
                    readerSettingsDao.updateSettings(resolved)
                } else {
                    readerSettingsDao.saveSettings(remoteSettings)
                }
            }

            // 3. Reading Sessions
            val sessionsSnapshot = firestore.collection("users")
                .document(parentAccountId)
                .collection("profiles")
                .document(profile.id)
                .collection("sessions")
                .get()
                .await()

            for (s in sessionsSnapshot.documents) {
                val sessionId = s.getString("id") ?: s.id
                val session = ReadingSessionEntity(
                    id = sessionId,
                    childProfileId = profile.id,
                    documentId = s.getString("documentId") ?: "",
                    durationSeconds = (s.getLong("durationSeconds") ?: 0L).toInt(),
                    wordsReadCount = (s.getLong("wordsReadCount") ?: 0L).toInt(),
                    startProgressPercent = (s.getLong("startProgressPercent") ?: 0L).toInt(),
                    endProgressPercent = (s.getLong("endProgressPercent") ?: 0L).toInt(),
                    lastReadWordIndex = (s.getLong("lastReadWordIndex") ?: 0L).toInt(),
                    createdAt = s.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = s.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isDeleted = s.getBoolean("isDeleted") ?: false
                )
                readingSessionDao.recordSession(session)
            }

            // 4. Practice Words
            val wordsSnapshot = firestore.collection("users")
                .document(parentAccountId)
                .collection("profiles")
                .document(profile.id)
                .collection("practiceWords")
                .get()
                .await()

            for (w in wordsSnapshot.documents) {
                val wordId = w.getString("id") ?: w.id
                val cleanWord = w.getString("cleanWord") ?: ""
                val remoteWord = WordMasteryEntity(
                    id = wordId,
                    childProfileId = profile.id,
                    word = w.getString("word") ?: cleanWord,
                    cleanWord = cleanWord,
                    script = w.getString("script") ?: "LATIN",
                    timesRead = (w.getLong("timesRead") ?: 1L).toInt(),
                    timesPracticed = (w.getLong("timesPracticed") ?: 0L).toInt(),
                    isMastered = w.getBoolean("isMastered") ?: false,
                    lastPracticedAt = w.getLong("lastPracticedAt") ?: System.currentTimeMillis(),
                    createdAt = w.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = w.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isSynced = true,
                    isDeleted = w.getBoolean("isDeleted") ?: false
                )

                val localWord = wordMasteryDao.getWord(profile.id, cleanWord)
                if (localWord != null) {
                    val resolved = SyncConflictResolver.resolveWordConflict(localWord, remoteWord)
                    wordMasteryDao.updateWord(resolved)
                } else {
                    wordMasteryDao.insertWord(remoteWord)
                }
            }
        }
    }

    // ==========================================
    // DELETION LOGIC (ACCOUNT & PROFILE DELETION)
    // ==========================================

    suspend fun deleteRemoteProfile(parentAccountId: String, profileId: String) = withContext(Dispatchers.IO) {
        if (parentAccountId.isBlank() || parentAccountId.startsWith("offline")) return@withContext

        try {
            // Delete all subcollections under profile
            val profileRef = firestore.collection("users")
                .document(parentAccountId)
                .collection("profiles")
                .document(profileId)

            val docs = profileRef.collection("documents").get().await()
            for (d in docs) d.reference.delete().await()

            val sessions = profileRef.collection("sessions").get().await()
            for (s in sessions) s.reference.delete().await()

            val words = profileRef.collection("practiceWords").get().await()
            for (w in words) w.reference.delete().await()

            val settings = profileRef.collection("settings").get().await()
            for (st in settings) st.reference.delete().await()

            profileRef.delete().await()
        } catch (_: Exception) {
            // Mark deleted locally
        }
    }

    suspend fun deleteRemoteAccountData(parentAccountId: String) = withContext(Dispatchers.IO) {
        if (parentAccountId.isBlank() || parentAccountId.startsWith("offline")) return@withContext

        try {
            val userRef = firestore.collection("users").document(parentAccountId)
            val profiles = userRef.collection("profiles").get().await()
            for (p in profiles) {
                deleteRemoteProfile(parentAccountId, p.id)
            }
            userRef.delete().await()
        } catch (_: Exception) {
            // Graceful deletion
        }
    }
}
