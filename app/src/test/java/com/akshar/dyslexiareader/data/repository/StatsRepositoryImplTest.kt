package com.akshar.dyslexiareader.data.repository

import com.akshar.dyslexiareader.data.local.dao.ChildProfileDao
import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.dao.ReadingSessionDao
import com.akshar.dyslexiareader.data.local.dao.TeacherNoteDao
import com.akshar.dyslexiareader.data.local.dao.WordMasteryDao
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.local.entity.ReadingSessionEntity
import com.akshar.dyslexiareader.data.local.entity.TeacherNoteEntity
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StatsRepositoryImplTest {

    private lateinit var fakeProfileDao: FakeChildProfileDao
    private lateinit var fakeSessionDao: FakeReadingSessionDao
    private lateinit var fakeMasteryDao: FakeWordMasteryDao
    private lateinit var fakeDocDao: FakeDocumentDao
    private lateinit var fakeNoteDao: FakeTeacherNoteDao
    private lateinit var repository: StatsRepositoryImpl

    private val testProfile = ChildProfileEntity(
        id = "prof-1",
        parentAccountId = "p-1",
        displayName = "Priya Rao",
        avatarIconName = "avatar_owl",
        pinType = "none",
        hashedPin = "",
        salt = "",
        preferredFontId = "opendyslexic",
        preferredThemeId = "paper_cream",
        gradeLevel = "Grade 4",
        isDeleted = false
    )

    @Before
    fun setup() {
        fakeProfileDao = FakeChildProfileDao()
        fakeSessionDao = FakeReadingSessionDao()
        fakeMasteryDao = FakeWordMasteryDao()
        fakeDocDao = FakeDocumentDao()
        fakeNoteDao = FakeTeacherNoteDao()

        repository = StatsRepositoryImpl(
            childProfileDao = fakeProfileDao,
            readingSessionDao = fakeSessionDao,
            wordMasteryDao = fakeMasteryDao,
            documentDao = fakeDocDao,
            teacherNoteDao = fakeNoteDao
        )

        fakeProfileDao.profiles.value = listOf(testProfile)
    }

    @Test
    fun `getStudentProgress returns null when profile does not exist or is deleted`() = runTest {
        val nullResult = repository.getStudentProgress("non-existent").first()
        assertNull(nullResult)

        fakeProfileDao.profiles.value = listOf(testProfile.copy(isDeleted = true))
        val deletedResult = repository.getStudentProgress("prof-1").first()
        assertNull(deletedResult)
    }

    @Test
    fun `getStudentProgress aggregates reading sessions, masteries, and notes`() = runTest {
        val now = System.currentTimeMillis()
        fakeSessionDao.sessions.value = listOf(
            ReadingSessionEntity(
                id = "s1",
                childProfileId = "prof-1",
                documentId = "doc1",
                durationSeconds = 600,
                wordsReadCount = 100,
                createdAt = now
            ),
            ReadingSessionEntity(
                id = "s2",
                childProfileId = "prof-1",
                documentId = "doc1",
                durationSeconds = 1200,
                wordsReadCount = 200,
                createdAt = now
            )
        )
        fakeMasteryDao.words.value = listOf(
            WordMasteryEntity(
                id = "w1",
                childProfileId = "prof-1",
                word = "elephant",
                cleanWord = "elephant",
                script = "latin",
                timesRead = 3,
                isMastered = true,
                lastPracticedAt = now
            )
        )
        fakeNoteDao.notes.value = listOf(
            TeacherNoteEntity("n1", "prof-1", "Super reader!", "Praise", "Teacher", now, now, false)
        )

        val stats = repository.getStudentProgress("prof-1").first()
        assertNotNull(stats)
        assertEquals("Priya Rao", stats!!.profile.displayName)
        assertEquals(1, stats.masteredWordsCount)
        assertEquals("elephant", stats.masteredWords.first().word)
        assertEquals(1, stats.teacherNotes.size)
        assertEquals("Super reader!", stats.teacherNotes.first().noteText)
    }

    @Test
    fun `addTeacherNote and deleteTeacherNote function correctly`() = runTest {
        repository.addTeacherNote("prof-1", "Needs more time with conjuncts", "Phonics", "Mrs. Rao")

        val notes = repository.getTeacherNotes("prof-1").first()
        assertEquals(1, notes.size)
        assertEquals("Needs more time with conjuncts", notes.first().noteText)
        assertEquals("Phonics", notes.first().category)

        repository.deleteTeacherNote(notes.first().id)
        val remaining = repository.getTeacherNotes("prof-1").first()
        assertTrue("Note should be removed/soft-deleted", remaining.isEmpty())
    }

    @Test
    fun `deleteStudentStats purges sessions, mastered words and teacher notes`() = runTest {
        val now = System.currentTimeMillis()
        fakeSessionDao.sessions.value = listOf(
            ReadingSessionEntity("s1", "prof-1", "doc1", 600, 100, 10, 20, 0, now, now, false)
        )
        fakeMasteryDao.words.value = listOf(
            WordMasteryEntity("w1", "prof-1", "elephant", "elephant", "latin", 3, 2, true, now, now, now, false, false)
        )
        fakeNoteDao.notes.value = listOf(
            TeacherNoteEntity("n1", "prof-1", "Super reader!", "Praise", "Teacher", now, now, false)
        )

        repository.deleteStudentStats("prof-1")

        assertTrue("Sessions cleared", fakeSessionDao.sessions.value.isEmpty())
        assertTrue("Mastery cleared", fakeMasteryDao.words.value.isEmpty())
        assertTrue("Notes cleared", fakeNoteDao.notes.value.isEmpty())
    }

    // --- Fake DAO Implementations ---
    class FakeChildProfileDao : ChildProfileDao {
        val profiles = MutableStateFlow<List<ChildProfileEntity>>(emptyList())
        override fun getProfilesForParent(parentId: String): Flow<List<ChildProfileEntity>> = profiles
        override fun getAllProfiles(): Flow<List<ChildProfileEntity>> = profiles
        override suspend fun getAllProfilesDirect(): List<ChildProfileEntity> = profiles.value
        override suspend fun getProfilesForParentDirect(parentId: String): List<ChildProfileEntity> = profiles.value
        override suspend fun getProfileById(profileId: String): ChildProfileEntity? = profiles.value.find { it.id == profileId }
        override fun observeProfileById(profileId: String): Flow<ChildProfileEntity?> = MutableStateFlow(profiles.value.find { it.id == profileId })
        override suspend fun getUnsyncedProfiles(): List<ChildProfileEntity> = emptyList()
        override suspend fun insertProfile(profile: ChildProfileEntity) { profiles.value = profiles.value + profile }
        override suspend fun updateProfile(profile: ChildProfileEntity) { profiles.value = profiles.value.map { if (it.id == profile.id) profile else it } }
        override suspend fun deleteProfile(profile: ChildProfileEntity) { profiles.value = profiles.value.filter { it.id != profile.id } }
        override suspend fun deleteProfileById(profileId: String) { profiles.value = profiles.value.filter { it.id != profileId } }
        override suspend fun clearAllProfiles() { profiles.value = emptyList() }
    }

    class FakeReadingSessionDao : ReadingSessionDao {
        val sessions = MutableStateFlow<List<ReadingSessionEntity>>(emptyList())
        override fun getSessionsForProfile(profileId: String): Flow<List<ReadingSessionEntity>> = sessions
        override suspend fun getSessionsForProfileDirect(profileId: String): List<ReadingSessionEntity> = sessions.value
        override suspend fun getAllSessions(): List<ReadingSessionEntity> = sessions.value
        override suspend fun recordSession(session: ReadingSessionEntity) { sessions.value = sessions.value + session }
        override suspend fun insertSessions(sessionsList: List<ReadingSessionEntity>) { sessions.value = sessions.value + sessionsList }
        override suspend fun deleteSessionsForProfile(profileId: String) { sessions.value = emptyList() }
    }

    class FakeWordMasteryDao : WordMasteryDao {
        val words = MutableStateFlow<List<WordMasteryEntity>>(emptyList())
        override fun getMasteredWords(profileId: String): Flow<List<WordMasteryEntity>> =
            MutableStateFlow(words.value.filter { it.childProfileId == profileId && it.isMastered })
        override suspend fun getMasteredWordsDirect(profileId: String): List<WordMasteryEntity> =
            words.value.filter { it.childProfileId == profileId && it.isMastered }
        override fun getAllWordsForProfile(profileId: String): Flow<List<WordMasteryEntity>> =
            MutableStateFlow(words.value.filter { it.childProfileId == profileId })
        override suspend fun getAllWordsForProfileDirect(profileId: String): List<WordMasteryEntity> =
            words.value.filter { it.childProfileId == profileId }
        override suspend fun getAllWordsDirect(): List<WordMasteryEntity> = words.value
        override suspend fun getWord(profileId: String, cleanWord: String): WordMasteryEntity? =
            words.value.find { it.childProfileId == profileId && it.cleanWord == cleanWord }
        override fun getMasteredCount(profileId: String): Flow<Int> =
            MutableStateFlow(words.value.count { it.childProfileId == profileId && it.isMastered })
        override suspend fun getUnsyncedWords(): List<WordMasteryEntity> = emptyList()
        override suspend fun insertWord(word: WordMasteryEntity) {
            words.value = words.value.filter { !(it.childProfileId == word.childProfileId && it.word == word.word) } + word
        }
        override suspend fun insertWords(wordsList: List<WordMasteryEntity>) { words.value = words.value + wordsList }
        override suspend fun updateWord(word: WordMasteryEntity) {
            words.value = words.value.map { if (it.id == word.id) word else it }
        }
        override suspend fun deleteWord(word: WordMasteryEntity) { words.value = words.value.filter { it.id != word.id } }
        override suspend fun deleteAllForProfile(profileId: String) { words.value = emptyList() }
        override suspend fun deleteWordsForProfile(profileId: String) { words.value = emptyList() }
        override suspend fun clearAllWords() { words.value = emptyList() }
    }

    class FakeDocumentDao : DocumentDao {
        override fun getDocumentsForProfile(profileId: String): Flow<List<DocumentEntity>> = MutableStateFlow(emptyList())
        override fun getFavoriteDocuments(profileId: String): Flow<List<DocumentEntity>> = MutableStateFlow(emptyList())
        override fun getDocumentsBySubject(profileId: String, subject: String): Flow<List<DocumentEntity>> = MutableStateFlow(emptyList())
        override suspend fun getDocumentsForProfileDirect(profileId: String): List<DocumentEntity> = emptyList()
        override suspend fun getAllDocumentsDirect(): List<DocumentEntity> = emptyList()
        override suspend fun getDocumentById(documentId: String): DocumentEntity? = null
        override suspend fun getDocumentByIdDirect(documentId: String): DocumentEntity? = null
        override fun observeDocumentById(documentId: String): Flow<DocumentEntity?> = MutableStateFlow(null)
        override fun searchDocuments(profileId: String, query: String): Flow<List<DocumentEntity>> = MutableStateFlow(emptyList())
        override suspend fun getUnsyncedDocuments(): List<DocumentEntity> = emptyList()
        override suspend fun insertDocument(document: DocumentEntity) {}
        override suspend fun insertDocuments(documents: List<DocumentEntity>) {}
        override suspend fun updateDocument(document: DocumentEntity) {}
        override suspend fun renameDocument(documentId: String, newTitle: String, updatedAt: Long) {}
        override suspend fun toggleFavorite(documentId: String, isFavorite: Boolean, updatedAt: Long) {}
        override suspend fun updateTags(documentId: String, tags: String, updatedAt: Long) {}
        override suspend fun updateReadingProgress(documentId: String, progressPercent: Int, lastWordIndex: Int, updatedAt: Long) {}
        override suspend fun softDeleteDocument(documentId: String, updatedAt: Long) {}
        override suspend fun restoreDocument(documentId: String, updatedAt: Long) {}
        override suspend fun deleteDocument(document: DocumentEntity) {}
        override suspend fun deleteDocumentById(documentId: String) {}
        override suspend fun deleteAllForProfile(profileId: String) {}
        override suspend fun clearAllDocuments() {}
    }

    class FakeTeacherNoteDao : TeacherNoteDao {
        val notes = MutableStateFlow<List<TeacherNoteEntity>>(emptyList())
        override fun getNotesForProfile(profileId: String): Flow<List<TeacherNoteEntity>> =
            MutableStateFlow(notes.value.filter { it.childProfileId == profileId && !it.isDeleted })
        override suspend fun getNotesForProfileDirect(profileId: String): List<TeacherNoteEntity> =
            notes.value.filter { it.childProfileId == profileId && !it.isDeleted }
        override suspend fun getNoteById(noteId: String): TeacherNoteEntity? = notes.value.find { it.id == noteId }
        override suspend fun insertNote(note: TeacherNoteEntity) { notes.value = notes.value + note }
        override suspend fun updateNote(note: TeacherNoteEntity) { notes.value = notes.value.map { if (it.id == note.id) note else it } }
        override suspend fun softDeleteNote(noteId: String, timestamp: Long) {
            notes.value = notes.value.filter { it.id != noteId }
        }
        override suspend fun deleteNotesForProfile(profileId: String) { notes.value = emptyList() }
    }
}
