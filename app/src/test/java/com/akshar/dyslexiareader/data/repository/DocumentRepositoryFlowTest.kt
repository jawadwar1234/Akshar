package com.akshar.dyslexiareader.data.repository

import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeDocumentDao : DocumentDao {
    val documents = mutableMapOf<String, DocumentEntity>()

    override fun getDocumentsForProfile(profileId: String): Flow<List<DocumentEntity>> {
        return flowOf(documents.values.filter { it.childProfileId == profileId && !it.isDeleted })
    }

    override fun getFavoriteDocuments(profileId: String): Flow<List<DocumentEntity>> {
        return flowOf(documents.values.filter { it.childProfileId == profileId && it.isFavorite && !it.isDeleted })
    }

    override fun getDocumentsBySubject(profileId: String, subject: String): Flow<List<DocumentEntity>> {
        return flowOf(documents.values.filter { it.childProfileId == profileId && it.subject == subject && !it.isDeleted })
    }

    override suspend fun getDocumentById(documentId: String): DocumentEntity? {
        val doc = documents[documentId]
        return if (doc?.isDeleted == false) doc else null
    }

    override fun observeDocumentById(documentId: String): Flow<DocumentEntity?> {
        return flowOf(documents[documentId])
    }

    override fun searchDocuments(profileId: String, query: String): Flow<List<DocumentEntity>> {
        return flowOf(
            documents.values.filter {
                it.childProfileId == profileId && !it.isDeleted &&
                    (it.title.contains(query, ignoreCase = true) || it.cleanText.contains(query, ignoreCase = true))
            }
        )
    }

    override suspend fun getUnsyncedDocuments(): List<DocumentEntity> {
        return documents.values.filter { !it.isSynced }
    }

    override suspend fun insertDocument(document: DocumentEntity) {
        documents[document.id] = document
    }

    override suspend fun insertDocuments(docs: List<DocumentEntity>) {
        docs.forEach { documents[it.id] = it }
    }

    override suspend fun updateDocument(document: DocumentEntity) {
        documents[document.id] = document
    }

    override suspend fun renameDocument(documentId: String, newTitle: String, updatedAt: Long) {
        documents[documentId]?.let {
            documents[documentId] = it.copy(title = newTitle, updatedAt = updatedAt)
        }
    }

    override suspend fun toggleFavorite(documentId: String, isFavorite: Boolean, updatedAt: Long) {
        documents[documentId]?.let {
            documents[documentId] = it.copy(isFavorite = isFavorite, updatedAt = updatedAt)
        }
    }

    override suspend fun updateTags(documentId: String, tags: String, updatedAt: Long) {
        documents[documentId]?.let {
            documents[documentId] = it.copy(tags = tags, updatedAt = updatedAt)
        }
    }

    override suspend fun updateReadingProgress(documentId: String, progressPercent: Int, lastWordIndex: Int, updatedAt: Long) {
        documents[documentId]?.let {
            documents[documentId] = it.copy(
                readingProgressPercent = progressPercent,
                lastReadWordIndex = lastWordIndex,
                updatedAt = updatedAt
            )
        }
    }

    override suspend fun softDeleteDocument(documentId: String, updatedAt: Long) {
        documents[documentId]?.let {
            documents[documentId] = it.copy(isDeleted = true, updatedAt = updatedAt)
        }
    }

    override suspend fun restoreDocument(documentId: String, updatedAt: Long) {
        documents[documentId]?.let {
            documents[documentId] = it.copy(isDeleted = false, updatedAt = updatedAt)
        }
    }

    override suspend fun deleteDocument(document: DocumentEntity) {
        documents.remove(document.id)
    }

    override suspend fun deleteDocumentById(documentId: String) {
        documents.remove(documentId)
    }

    override suspend fun deleteAllForProfile(profileId: String) {
        documents.values.removeAll { it.childProfileId == profileId }
    }

    override suspend fun clearAllDocuments() {
        documents.clear()
    }
}

class DocumentRepositoryFlowTest {

    private lateinit var fakeDao: FakeDocumentDao
    private lateinit var repository: DocumentRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeDocumentDao()
        repository = DocumentRepositoryImpl(fakeDao)
    }

    @Test
    fun `scan read save and reopen preserves document reading progress`() = runBlocking {
        val docId = "doc_123"
        val profileId = "profile_aarav"
        val sampleDoc = DocumentEntity(
            id = docId,
            childProfileId = profileId,
            title = "Science Chapter 1",
            rawOcrText = "Plants make food by photosynthesis.",
            cleanText = "Plants make food by photosynthesis.",
            detectedScript = "LATIN",
            subject = "Science",
            wordCount = 5,
            readingProgressPercent = 60,
            lastReadWordIndex = 3
        )

        // Save
        repository.saveDocument(sampleDoc)

        // Reopen from repository
        val retrieved = repository.getDocumentById(docId)

        assertNotNull(retrieved)
        assertEquals("Science Chapter 1", retrieved?.title)
        assertEquals(60, retrieved?.readingProgressPercent)
        assertEquals(3, retrieved?.lastReadWordIndex)
        assertEquals("Science", retrieved?.subject)
    }

    @Test
    fun `exportDataAsJson formats all documents for student profile`() = runBlocking {
        val profileId = "profile_aarav"
        val doc = DocumentEntity(
            id = "d1",
            childProfileId = profileId,
            title = "Hindi Lesson 1",
            rawOcrText = "सूर्य पूर्व से निकलता है।",
            cleanText = "सूर्य पूर्व से निकलता है।",
            detectedScript = "DEVANAGARI",
            subject = "Hindi",
            wordCount = 5
        )

        repository.saveDocument(doc)

        val json = repository.exportDataAsJson(profileId)
        assertTrue(json.contains("Hindi Lesson 1"))
        assertTrue(json.contains("DEVANAGARI"))
        assertTrue(json.contains("सूर्य पूर्व से निकलता है।"))
    }

    @Test
    fun `softDelete and restore toggle document visibility`() = runBlocking {
        val docId = "doc_test_delete"
        val doc = DocumentEntity(
            id = docId,
            childProfileId = "p1",
            title = "Test Delete",
            rawOcrText = "Test",
            cleanText = "Test",
            detectedScript = "LATIN"
        )

        repository.saveDocument(doc)
        assertNotNull(repository.getDocumentById(docId))

        // Soft delete
        repository.softDeleteDocument(docId)
        assertEquals(null, repository.getDocumentById(docId))

        // Restore / Undo
        repository.restoreDocument(docId)
        assertNotNull(repository.getDocumentById(docId))
    }
}
