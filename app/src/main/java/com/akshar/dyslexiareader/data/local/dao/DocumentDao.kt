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

    @Query("SELECT * FROM documents WHERE childProfileId = :profileId ORDER BY updatedAt DESC")
    fun getDocumentsForProfile(profileId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    suspend fun getDocumentById(documentId: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    fun observeDocumentById(documentId: String): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE childProfileId = :profileId AND (title LIKE '%' || :query || '%' OR cleanText LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun searchDocuments(profileId: String, query: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isSynced = 0")
    suspend fun getUnsyncedDocuments(): List<DocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentEntity>)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :documentId")
    suspend fun deleteDocumentById(documentId: String)

    @Query("DELETE FROM documents WHERE childProfileId = :profileId")
    suspend fun deleteAllForProfile(profileId: String)

    @Query("DELETE FROM documents")
    suspend fun clearAllDocuments()
}
