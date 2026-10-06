package com.akshar.dyslexiareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "word_mastery",
    indices = [
        Index(value = ["childProfileId", "word"], unique = true),
        Index(value = ["childProfileId", "isMastered"]),
        Index(value = ["isDeleted"])
    ]
)
data class WordMasteryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val childProfileId: String,
    val word: String,
    val cleanWord: String,
    val script: String,
    val timesRead: Int = 1,
    val timesPracticed: Int = 0,
    val isMastered: Boolean = false,
    val lastPracticedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)

typealias PracticeWordEntity = WordMasteryEntity
