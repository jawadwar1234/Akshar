package com.akshar.dyslexiareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "teacher_notes",
    indices = [
        Index(value = ["childProfileId"]),
        Index(value = ["createdAt"])
    ]
)
data class TeacherNoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val childProfileId: String,
    val noteText: String,
    val category: String = "General", // "General", "Phonics", "Praise", "Observation"
    val authorName: String = "Teacher / Parent",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
