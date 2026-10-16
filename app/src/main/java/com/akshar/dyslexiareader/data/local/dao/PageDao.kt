package com.akshar.dyslexiareader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akshar.dyslexiareader.data.local.entity.PageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {

    @Query("SELECT * FROM pages WHERE documentId = :documentId AND isDeleted = 0 ORDER BY pageNumber ASC")
    fun getPagesForDocument(documentId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE documentId = :documentId AND isDeleted = 0 ORDER BY pageNumber ASC")
    suspend fun getPagesForDocumentDirect(documentId: String): List<PageEntity>

    @Query("SELECT * FROM pages")
    suspend fun getAllPages(): List<PageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<PageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageEntity)

    @Update
    suspend fun updatePage(page: PageEntity)

    @Query("UPDATE pages SET isDeleted = 1, updatedAt = :updatedAt WHERE documentId = :documentId")
    suspend fun softDeletePagesForDocument(documentId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM pages WHERE documentId = :documentId")
    suspend fun deletePagesForDocument(documentId: String)
}
