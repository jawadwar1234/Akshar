package com.akshar.dyslexiareader

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.akshar.dyslexiareader.data.local.AppDatabase
import com.akshar.dyslexiareader.data.local.dao.ChildProfileDao
import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.dao.PageDao
import com.akshar.dyslexiareader.data.local.dao.ReaderSettingsDao
import com.akshar.dyslexiareader.data.local.dao.ReadingSessionDao
import com.akshar.dyslexiareader.data.local.dao.WordMasteryDao
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.local.entity.PageEntity
import com.akshar.dyslexiareader.data.local.entity.ReaderSettingsEntity
import com.akshar.dyslexiareader.data.local.entity.ReadingSessionEntity
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity
import com.akshar.dyslexiareader.data.repository.DocumentRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * Instrumented test for Milestone 3 Acceptance Flow:
 * Scan -> Read -> Save -> Close App -> Reopen from Library at the same position.
 * Also verifies all 6 Room entities (Profile, Document, Page, ReaderSettings, ReadingSession, PracticeWord).
 */
@RunWith(AndroidJUnit4::class)
class DocumentPersistenceFlowInstrumentedTest {

    private lateinit var db: AppDatabase
    private lateinit var documentDao: DocumentDao
    private lateinit var profileDao: ChildProfileDao
    private lateinit var pageDao: PageDao
    private lateinit var settingsDao: ReaderSettingsDao
    private lateinit var sessionDao: ReadingSessionDao
    private lateinit var wordMasteryDao: WordMasteryDao
    private lateinit var repository: DocumentRepositoryImpl
    private lateinit var appContext: Context

    @Before
    fun createDb() {
        appContext = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        documentDao = db.documentDao()
        profileDao = db.childProfileDao()
        pageDao = db.pageDao()
        settingsDao = db.readerSettingsDao()
        sessionDao = db.readingSessionDao()
        wordMasteryDao = db.wordMasteryDao()

        repository = DocumentRepositoryImpl(documentDao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun acceptanceFlow_scan_read_save_closeApp_reopenAtSamePosition() = runBlocking {
        val profileId = UUID.randomUUID().toString()
        val docId = UUID.randomUUID().toString()

        // 1. Setup Active Child Profile
        val childProfile = ChildProfileEntity(
            id = profileId,
            parentAccountId = "parent_account_1",
            displayName = "Aarav",
            pinType = "PICTURE",
            hashedPin = "mock_hashed_pin",
            salt = "mock_salt",
            gradeLevel = "Grade 4",
            preferredThemeId = "cream",
            preferredFontSizeSp = 24f
        )
        profileDao.insertProfile(childProfile)

        // 2. SCAN: Simulate scanning a textbook page and storing image path in app-private storage
        val privateScansDir = File(appContext.filesDir, "scanned_documents").apply {
            if (!exists()) mkdirs()
        }
        val scanImageFile = File(privateScansDir, "doc_$docId.jpg")
        scanImageFile.writeText("fake_jpeg_binary_data")

        val scannedText = "The solar system consists of the Sun and the planets orbiting around it. Earth is the third planet."
        val initialDoc = DocumentEntity(
            id = docId,
            childProfileId = profileId,
            title = "Solar System Chapter 3",
            rawOcrText = scannedText,
            cleanText = scannedText,
            detectedScript = "LATIN",
            imagePath = scanImageFile.absolutePath,
            subject = "Science",
            isFavorite = true,
            tags = "Science, Planets, Exam",
            wordCount = 16,
            readingProgressPercent = 0,
            lastReadWordIndex = 0
        )
        repository.saveDocument(initialDoc)

        // 3. READ & PROGRESS: Student reads along up to word index 10 (out of 16 words ~ 62%)
        val wordsRead = 10
        val progressPercent = (wordsRead * 100) / 16 // 62%
        repository.updateProgress(
            documentId = docId,
            progressPercent = progressPercent,
            lastReadWordIndex = wordsRead
        )

        // Record reading session entity
        val readingSession = ReadingSessionEntity(
            id = UUID.randomUUID().toString(),
            childProfileId = profileId,
            documentId = docId,
            durationSeconds = 120,
            wordsReadCount = wordsRead,
            startProgressPercent = 0,
            endProgressPercent = progressPercent,
            lastReadWordIndex = wordsRead
        )
        sessionDao.recordSession(readingSession)

        // 4. CLOSE APP SIMULATION:
        // In Android, closing the app destroys view models and in-memory caches.
        // We simulate this by creating a brand new repository instance directly pointing to persistent DB.
        val freshRepository = DocumentRepositoryImpl(db.documentDao())

        // 5. REOPEN FROM LIBRARY:
        // Load library documents for profile
        val libraryDocs = freshRepository.getDocumentsForProfile(profileId).first()
        assertEquals(1, libraryDocs.size)

        val reopenedDoc = freshRepository.getDocumentById(docId)
        assertNotNull("Reopened document should exist in database", reopenedDoc)
        assertEquals("Solar System Chapter 3", reopenedDoc?.title)
        assertEquals("Science", reopenedDoc?.subject)
        assertEquals(true, reopenedDoc?.isFavorite)
        assertEquals("Science, Planets, Exam", reopenedDoc?.tags)
        assertEquals("LATIN", reopenedDoc?.detectedScript)
        assertEquals(scanImageFile.absolutePath, reopenedDoc?.imagePath)

        // Verify the exact reading position and progress were preserved!
        assertEquals(62, reopenedDoc?.readingProgressPercent)
        assertEquals(10, reopenedDoc?.lastReadWordIndex)

        // Clean up mock file
        scanImageFile.delete()
    }

    @Test
    fun verifyAllSixEntities_crudOperations() = runBlocking {
        val profileId = UUID.randomUUID().toString()
        val docId = UUID.randomUUID().toString()

        // 1. Profile Entity
        val profile = ChildProfileEntity(
            id = profileId,
            parentAccountId = "parent_001",
            displayName = "Ananya",
            hashedPin = "hash123",
            salt = "salt123"
        )
        profileDao.insertProfile(profile)
        val loadedProfile = profileDao.getProfileById(profileId)
        assertNotNull(loadedProfile)
        assertEquals("Ananya", loadedProfile?.displayName)

        // 2. Document Entity
        val doc = DocumentEntity(
            id = docId,
            childProfileId = profileId,
            title = "Marathi Kavita",
            rawOcrText = "सुंदर ते ध्यान उभे विटेवरी।",
            cleanText = "सुंदर ते ध्यान उभे विटेवरी।",
            detectedScript = "DEVANAGARI",
            subject = "Marathi",
            wordCount = 5
        )
        documentDao.insertDocument(doc)
        val loadedDoc = documentDao.getDocumentById(docId)
        assertNotNull(loadedDoc)
        assertEquals("Marathi", loadedDoc?.subject)

        // 3. Page Entity (Multi-page support)
        val page1 = PageEntity(
            id = UUID.randomUUID().toString(),
            documentId = docId,
            pageNumber = 1,
            rawOcrText = "सुंदर ते ध्यान",
            cleanText = "सुंदर ते ध्यान",
            detectedScript = "DEVANAGARI",
            wordCount = 3
        )
        val page2 = PageEntity(
            id = UUID.randomUUID().toString(),
            documentId = docId,
            pageNumber = 2,
            rawOcrText = "उभे विटेवरी।",
            cleanText = "उभे विटेवरी।",
            detectedScript = "DEVANAGARI",
            wordCount = 2
        )
        pageDao.insertPages(listOf(page1, page2))
        val pages = pageDao.getPagesForDocument(docId).first()
        assertEquals(2, pages.size)
        assertEquals(1, pages[0].pageNumber)
        assertEquals(2, pages[1].pageNumber)

        // 4. ReaderSettings Entity
        val settings = ReaderSettingsEntity(
            id = UUID.randomUUID().toString(),
            childProfileId = profileId,
            theme = "sepia",
            fontSize = 26f,
            lineSpacing = 1.8f,
            letterSpacing = 0.12f,
            wordSpacing = 0.15f,
            marginWidth = 20,
            rulerEnabled = true,
            rulerWindowLines = 3,
            syllablesEnabled = true,
            chunkingPaletteIndex = 1
        )
        settingsDao.saveSettings(settings)
        val loadedSettings = settingsDao.getSettingsForProfile(profileId).first()
        assertNotNull(loadedSettings)
        assertEquals("sepia", loadedSettings?.theme)
        assertTrue(loadedSettings?.rulerEnabled == true)
        assertEquals(3, loadedSettings?.rulerWindowLines)

        // 5. ReadingSession Entity
        val session = ReadingSessionEntity(
            id = UUID.randomUUID().toString(),
            childProfileId = profileId,
            documentId = docId,
            durationSeconds = 180,
            wordsReadCount = 15,
            startProgressPercent = 0,
            endProgressPercent = 100,
            lastReadWordIndex = 5
        )
        sessionDao.recordSession(session)
        val sessions = sessionDao.getSessionsForProfile(profileId).first()
        assertEquals(1, sessions.size)
        assertEquals(180, sessions[0].durationSeconds)

        // 6. WordMastery Entity (PracticeWord)
        val word = WordMasteryEntity(
            id = UUID.randomUUID().toString(),
            childProfileId = profileId,
            word = "विटेवरी",
            cleanWord = "विटेवरी",
            script = "DEVANAGARI",
            timesRead = 3,
            timesPracticed = 2,
            isMastered = true
        )
        wordMasteryDao.insertWord(word)
        val masteredWords = wordMasteryDao.getMasteredWords(profileId).first()
        assertEquals(1, masteredWords.size)
        assertEquals("विटेवरी", masteredWords[0].cleanWord)
        assertTrue(masteredWords[0].isMastered)
    }

    @Test
    fun libraryFeatures_search_filter_rename_deleteWithUndo() = runBlocking {
        val profileId = "profile_test_lib"
        val doc1 = DocumentEntity(
            id = "d1",
            childProfileId = profileId,
            title = "Photosynthesis in Green Plants",
            rawOcrText = "Chlorophyll absorbs sunlight",
            cleanText = "Chlorophyll absorbs sunlight",
            detectedScript = "LATIN",
            subject = "Science",
            tags = "Biology, Exam",
            isFavorite = true
        )
        val doc2 = DocumentEntity(
            id = "d2",
            childProfileId = profileId,
            title = "Indian History 1857",
            rawOcrText = "Freedom struggle started in Meerut",
            cleanText = "Freedom struggle started in Meerut",
            detectedScript = "LATIN",
            subject = "Social Studies",
            tags = "History, Chapter 2",
            isFavorite = false
        )
        repository.saveDocument(doc1)
        repository.saveDocument(doc2)

        // 1. Search by title
        val searchResults1 = repository.searchDocuments(profileId, "Chlorophyll").first()
        assertEquals(1, searchResults1.size)
        assertEquals("d1", searchResults1[0].id)

        // 2. Filter by Favorites
        val favorites = repository.getFavoriteDocuments(profileId).first()
        assertEquals(1, favorites.size)
        assertEquals("d1", favorites[0].id)

        // 3. Filter by Subject
        val scienceDocs = repository.getDocumentsBySubject(profileId, "Science").first()
        assertEquals(1, scienceDocs.size)
        assertEquals("d1", scienceDocs[0].id)

        // 4. Rename
        repository.renameDocument("d1", "Advanced Photosynthesis")
        val renamedDoc = repository.getDocumentById("d1")
        assertEquals("Advanced Photosynthesis", renamedDoc?.title)

        // 5. Update Tags
        repository.updateTags("d1", "Biology, Photosynthesis, Final Exam")
        val updatedTagDoc = repository.getDocumentById("d1")
        assertEquals("Biology, Photosynthesis, Final Exam", updatedTagDoc?.tags)

        // 6. Delete with Undo
        repository.softDeleteDocument("d2")
        assertNull(repository.getDocumentById("d2"))
        val docsAfterDelete = repository.getDocumentsForProfile(profileId).first()
        assertEquals(1, docsAfterDelete.size)

        // Undo
        repository.restoreDocument("d2")
        val restoredDoc = repository.getDocumentById("d2")
        assertNotNull(restoredDoc)
        assertEquals("Indian History 1857", restoredDoc?.title)
    }
}
