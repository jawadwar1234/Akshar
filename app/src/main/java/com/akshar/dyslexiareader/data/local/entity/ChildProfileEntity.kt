package com.akshar.dyslexiareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "child_profiles",
    indices = [
        Index(value = ["parentAccountId"]),
        Index(value = ["isDeleted"])
    ]
)
data class ChildProfileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val parentAccountId: String,
    val displayName: String,
    val avatarIconName: String = "avatar_1",
    val pinType: String = "PICTURE", // "PICTURE" or "NUMBER"
    val hashedPin: String,
    val salt: String,
    val gradeLevel: String = "Grade 3",
    val preferredThemeId: String = "cream",
    val preferredFontId: String = "lexend",
    val preferredFontSizeSp: Float = 22f,
    val preferredLineSpacing: Float = 1.6f,
    val preferredLetterSpacing: Float = 0.08f,
    val preferredTtsSpeed: Float = 1.0f,
    val preferredTtsPitch: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)
