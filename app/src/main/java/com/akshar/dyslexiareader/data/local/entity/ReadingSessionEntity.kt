package com.akshar.dyslexiareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "reading_sessions",
    indices = [
        Index(value = ["childProfileId"]),
        Index(value = ["documentId"]),
        Index(value = ["createdAt"])
    ]
)
data class ReadingSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val childProfileId: String,
    val documentId: String,
    val durationSeconds: Int = 0,
    val wordsReadCount: Int = 0,
    val startProgressPercent: Int = 0,
    val endProgressPercent: Int = 0,
    val lastReadWordIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
