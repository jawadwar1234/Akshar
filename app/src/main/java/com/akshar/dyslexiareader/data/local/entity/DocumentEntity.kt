package com.akshar.dyslexiareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    indices = [
        Index(value = ["childProfileId"]),
        Index(value = ["updatedAt"])
    ]
)
data class DocumentEntity(
    @PrimaryKey
    val id: String,
    val childProfileId: String,
    val title: String,
    val rawOcrText: String,
    val cleanText: String,
    val detectedScript: String,
    val imagePath: String? = null,
    val wordCount: Int = 0,
    val readingProgressPercent: Int = 0,
    val lastReadWordIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
