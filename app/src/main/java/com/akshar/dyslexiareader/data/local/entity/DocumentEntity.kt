package com.akshar.dyslexiareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "documents",
    indices = [
        Index(value = ["childProfileId"]),
        Index(value = ["updatedAt"]),
        Index(value = ["isFavorite"]),
        Index(value = ["subject"]),
        Index(value = ["isDeleted"])
    ]
)
data class DocumentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val childProfileId: String,
    val title: String,
    val rawOcrText: String,
    val cleanText: String,
    val detectedScript: String,
    val imagePath: String? = null,
    val subject: String = "General",
    val isFavorite: Boolean = false,
    val tags: String = "", // Comma-separated tags, e.g. "Biology, Chapter 3"
    val wordCount: Int = 0,
    val readingProgressPercent: Int = 0,
    val lastReadWordIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)
