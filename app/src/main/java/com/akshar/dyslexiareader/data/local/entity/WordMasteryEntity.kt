package com.akshar.dyslexiareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "word_mastery",
    indices = [
        Index(value = ["childProfileId", "word"], unique = true),
        Index(value = ["childProfileId", "isMastered"])
    ]
)
data class WordMasteryEntity(
    @PrimaryKey
    val id: String,
    val childProfileId: String,
    val word: String,
    val cleanWord: String,
    val script: String,
    val timesRead: Int = 1,
    val timesPracticed: Int = 0,
    val isMastered: Boolean = false,
    val lastPracticedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
