package com.akshar.dyslexiareader.data.sync

import com.akshar.dyslexiareader.data.local.AppDatabase
import com.akshar.dyslexiareader.data.local.dao.ChildProfileDao
import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.dao.PageDao
import com.akshar.dyslexiareader.data.local.dao.ReaderSettingsDao
import com.akshar.dyslexiareader.data.local.dao.ReadingSessionDao
import com.akshar.dyslexiareader.data.local.dao.WordMasteryDao
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.local.entity.ReaderSettingsEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DataExportManagerTest {

    private lateinit var appDatabase: AppDatabase
    private lateinit var childProfileDao: ChildProfileDao
    private lateinit var documentDao: DocumentDao
    private lateinit var pageDao: PageDao
    private lateinit var readerSettingsDao: ReaderSettingsDao
    private lateinit var readingSessionDao: ReadingSessionDao
    private lateinit var wordMasteryDao: WordMasteryDao
    private lateinit var dataExportManager: DataExportManager

    @Before
    fun setUp() {
        appDatabase = mockk()
        childProfileDao = mockk()
        documentDao = mockk()
        pageDao = mockk()
        readerSettingsDao = mockk()
        readingSessionDao = mockk()
        wordMasteryDao = mockk()

        coEvery { appDatabase.childProfileDao() } returns childProfileDao
        coEvery { appDatabase.documentDao() } returns documentDao
        coEvery { appDatabase.pageDao() } returns pageDao
        coEvery { appDatabase.readerSettingsDao() } returns readerSettingsDao
        coEvery { appDatabase.readingSessionDao() } returns readingSessionDao
        coEvery { appDatabase.wordMasteryDao() } returns wordMasteryDao

        dataExportManager = DataExportManager(appDatabase)
    }

    @Test
    fun exportDataToJsonString_containsAllEntities() = runBlocking {
        val testProfile = ChildProfileEntity(
            id = "p1",
            parentAccountId = "parent123",
            displayName = "Diya",
            gradeLevel = "Grade 4",
            hashedPin = "pin",
            salt = "salt"
        )
        val testDoc = DocumentEntity(
            id = "d1",
            childProfileId = "p1",
            title = "History Chapter 1",
            rawOcrText = "Raw",
            cleanText = "Clean",
            detectedScript = "LATIN"
        )
        val testSetting = ReaderSettingsEntity(
            id = "s1",
            childProfileId = "p1",
            theme = "cream",
            fontSize = 24f
        )

        coEvery { childProfileDao.getAllProfilesDirect() } returns listOf(testProfile)
        coEvery { documentDao.getAllDocumentsDirect() } returns listOf(testDoc)
        coEvery { pageDao.getAllPages() } returns emptyList()
        coEvery { readerSettingsDao.getAllSettings() } returns listOf(testSetting)
        coEvery { readingSessionDao.getAllSessions() } returns emptyList()
        coEvery { wordMasteryDao.getAllWordsDirect() } returns emptyList()

        val jsonStr = dataExportManager.exportDataToJsonString("parent123")
        val json = JSONObject(jsonStr)

        assertEquals("1.0", json.getString("version"))
        assertEquals("parent123", json.getString("parentAccountId"))
        assertTrue(json.has("exportedAt"))

        val profilesArray = json.getJSONArray("profiles")
        assertEquals(1, profilesArray.length())
        assertEquals("Diya", profilesArray.getJSONObject(0).getString("displayName"))

        val docsArray = json.getJSONArray("documents")
        assertEquals(1, docsArray.length())
        assertEquals("History Chapter 1", docsArray.getJSONObject(0).getString("title"))

        val settingsArray = json.getJSONArray("readerSettings")
        assertEquals(1, settingsArray.length())
        assertEquals("cream", settingsArray.getJSONObject(0).getString("theme"))
    }
}
