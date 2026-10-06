package com.akshar.dyslexiareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "pages",
    indices = [
        Index(value = ["documentId"]),
        Index(value = ["pageNumber"])
    ]
)
data class PageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val pageNumber: Int,
    val imagePath: String? = null,
    val rawOcrText: String = "",
    val cleanText: String = "",
    val detectedScript: String = "LATIN",
    val wordCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
