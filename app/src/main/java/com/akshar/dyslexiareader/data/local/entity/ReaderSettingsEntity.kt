package com.akshar.dyslexiareader.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "reader_settings",
    indices = [
        Index(value = ["childProfileId"], unique = true)
    ]
)
data class ReaderSettingsEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val childProfileId: String,
    val theme: String = "cream",
    val fontSize: Float = 22f,
    val lineSpacing: Float = 1.6f,
    val letterSpacing: Float = 0.08f,
    val wordSpacing: Float = 0.1f,
    val marginWidth: Int = 16,
    val fontFamily: String = "lexend",
    val rulerEnabled: Boolean = false,
    val rulerWindowLines: Int = 1,
    val bionicEnabled: Boolean = false,
    val syllablesEnabled: Boolean = false,
    val chunkingPaletteIndex: Int = 0,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
