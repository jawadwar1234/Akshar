package com.akshar.dyslexiareader.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents WHERE childProfileId = :profileId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getDocumentsForProfile(profileId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE childProfileId = :profileId AND isFavorite = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getFavoriteDocuments(profileId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE childProfileId = :profileId AND subject = :subject AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getDocumentsBySubject(profileId: String, subject: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE childProfileId = :profileId ORDER BY updatedAt DESC")
    suspend fun getDocumentsForProfileDirect(profileId: String): List<DocumentEntity>

    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    suspend fun getAllDocumentsDirect(): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE id = :documentId AND isDeleted = 0 LIMIT 1")
    suspend fun getDocumentById(documentId: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    suspend fun getDocumentByIdDirect(documentId: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    fun observeDocumentById(documentId: String): Flow<DocumentEntity?>

    @Query("""
        SELECT * FROM documents 
        WHERE childProfileId = :profileId 
        AND isDeleted = 0 
        AND (title LIKE '%' || :query || '%' OR cleanText LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') 
        ORDER BY updatedAt DESC
    """)
    fun searchDocuments(profileId: String, query: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isSynced = 0")
    suspend fun getUnsyncedDocuments(): List<DocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentEntity>)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("UPDATE documents SET title = :newTitle, updatedAt = :updatedAt, isSynced = 0 WHERE id = :documentId")
    suspend fun renameDocument(documentId: String, newTitle: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET isFavorite = :isFavorite, updatedAt = :updatedAt, isSynced = 0 WHERE id = :documentId")
    suspend fun toggleFavorite(documentId: String, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET tags = :tags, updatedAt = :updatedAt, isSynced = 0 WHERE id = :documentId")
    suspend fun updateTags(documentId: String, tags: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET readingProgressPercent = :progressPercent, lastReadWordIndex = :lastWordIndex, updatedAt = :updatedAt WHERE id = :documentId")
    suspend fun updateReadingProgress(documentId: String, progressPercent: Int, lastWordIndex: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET isDeleted = 1, updatedAt = :updatedAt, isSynced = 0 WHERE id = :documentId")
    suspend fun softDeleteDocument(documentId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET isDeleted = 0, updatedAt = :updatedAt, isSynced = 0 WHERE id = :documentId")
    suspend fun restoreDocument(documentId: String, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :documentId")
    suspend fun deleteDocumentById(documentId: String)

    @Query("DELETE FROM documents WHERE childProfileId = :profileId")
    suspend fun deleteAllForProfile(profileId: String)

    @Query("DELETE FROM documents")
    suspend fun clearAllDocuments()
}
