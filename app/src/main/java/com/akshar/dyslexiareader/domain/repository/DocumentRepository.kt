package com.akshar.dyslexiareader.domain.repository

import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getDocumentsForProfile(profileId: String): Flow<List<DocumentEntity>>
    fun getFavoriteDocuments(profileId: String): Flow<List<DocumentEntity>>
    fun getDocumentsBySubject(profileId: String, subject: String): Flow<List<DocumentEntity>>
    suspend fun getDocumentById(documentId: String): DocumentEntity?
    fun observeDocumentById(documentId: String): Flow<DocumentEntity?>
    fun searchDocuments(profileId: String, query: String): Flow<List<DocumentEntity>>
    suspend fun saveDocument(document: DocumentEntity)
    suspend fun renameDocument(documentId: String, newTitle: String)
    suspend fun toggleFavorite(documentId: String, isFavorite: Boolean)
    suspend fun updateTags(documentId: String, tags: String)
    suspend fun updateProgress(documentId: String, progressPercent: Int, lastReadWordIndex: Int)
    suspend fun softDeleteDocument(documentId: String)
    suspend fun restoreDocument(documentId: String)
    suspend fun deleteDocument(documentId: String)
    suspend fun exportDataAsJson(profileId: String): String
    suspend fun exportDataAsText(profileId: String): String
}
