package com.akshar.dyslexiareader.domain.repository

import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getDocumentsForProfile(profileId: String): Flow<List<DocumentEntity>>
    suspend fun getDocumentById(documentId: String): DocumentEntity?
    fun observeDocumentById(documentId: String): Flow<DocumentEntity?>
    fun searchDocuments(profileId: String, query: String): Flow<List<DocumentEntity>>
    suspend fun saveDocument(document: DocumentEntity)
    suspend fun updateProgress(documentId: String, progressPercent: Int, lastReadWordIndex: Int)
    suspend fun deleteDocument(documentId: String)
    suspend fun exportDataAsJson(profileId: String): String
    suspend fun exportDataAsText(profileId: String): String
}
