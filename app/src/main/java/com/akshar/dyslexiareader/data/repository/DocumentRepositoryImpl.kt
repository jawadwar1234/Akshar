package com.akshar.dyslexiareader.data.repository

import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun getDocumentsForProfile(profileId: String): Flow<List<DocumentEntity>> {
        return documentDao.getDocumentsForProfile(profileId)
    }

    override suspend fun getDocumentById(documentId: String): DocumentEntity? {
        return documentDao.getDocumentById(documentId)
    }

    override fun observeDocumentById(documentId: String): Flow<DocumentEntity?> {
        return documentDao.observeDocumentById(documentId)
    }

    override fun searchDocuments(profileId: String, query: String): Flow<List<DocumentEntity>> {
        return documentDao.searchDocuments(profileId, query)
    }

    override suspend fun saveDocument(document: DocumentEntity) {
        documentDao.insertDocument(document)
    }

    override suspend fun updateProgress(documentId: String, progressPercent: Int, lastReadWordIndex: Int) {
        val doc = documentDao.getDocumentById(documentId)
        if (doc != null) {
            val updated = doc.copy(
                readingProgressPercent = progressPercent,
                lastReadWordIndex = lastReadWordIndex,
                updatedAt = System.currentTimeMillis()
            )
            documentDao.updateDocument(updated)
        }
    }

    override suspend fun deleteDocument(documentId: String) {
        documentDao.deleteDocumentById(documentId)
    }

    override suspend fun exportDataAsJson(profileId: String): String {
        val docs = documentDao.getDocumentsForProfile(profileId).first()
        val builder = StringBuilder("[\n")
        docs.forEachIndexed { index, doc ->
            builder.append("  {\n")
            builder.append("    \"id\": \"${doc.id}\",\n")
            builder.append("    \"title\": \"${doc.title.replace("\"", "\\\"")}\",\n")
            builder.append("    \"script\": \"${doc.detectedScript}\",\n")
            builder.append("    \"wordCount\": ${doc.wordCount},\n")
            builder.append("    \"progressPercent\": ${doc.readingProgressPercent},\n")
            builder.append("    \"text\": \"${doc.cleanText.replace("\"", "\\\"").replace("\n", "\\n")}\"\n")
            builder.append("  }${if (index < docs.size - 1) "," else ""}\n")
        }
        builder.append("]")
        return builder.toString()
    }

    override suspend fun exportDataAsText(profileId: String): String {
        val docs = documentDao.getDocumentsForProfile(profileId).first()
        val builder = StringBuilder("AKSHAR DYSLEXIA READER - EXPORTED TEXTBOOKS\n")
        builder.append("===================================================\n\n")
        docs.forEachIndexed { index, doc ->
            builder.append("DOCUMENT ${index + 1}: ${doc.title}\n")
            builder.append("Script: ${doc.detectedScript} | Words: ${doc.wordCount} | Progress: ${doc.readingProgressPercent}%\n")
            builder.append("---------------------------------------------------\n")
            builder.append(doc.cleanText)
            builder.append("\n\n===================================================\n\n")
        }
        return builder.toString()
    }
}
