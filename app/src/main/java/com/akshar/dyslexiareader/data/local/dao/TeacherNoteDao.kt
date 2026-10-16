package com.akshar.dyslexiareader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akshar.dyslexiareader.data.local.entity.TeacherNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherNoteDao {

    @Query("SELECT * FROM teacher_notes WHERE childProfileId = :profileId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getNotesForProfile(profileId: String): Flow<List<TeacherNoteEntity>>

    @Query("SELECT * FROM teacher_notes WHERE childProfileId = :profileId AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getNotesForProfileDirect(profileId: String): List<TeacherNoteEntity>

    @Query("SELECT * FROM teacher_notes WHERE id = :noteId AND isDeleted = 0")
    suspend fun getNoteById(noteId: String): TeacherNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: TeacherNoteEntity)

    @Update
    suspend fun updateNote(note: TeacherNoteEntity)

    @Query("UPDATE teacher_notes SET isDeleted = 1, updatedAt = :timestamp WHERE id = :noteId")
    suspend fun softDeleteNote(noteId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM teacher_notes WHERE childProfileId = :profileId")
    suspend fun deleteNotesForProfile(profileId: String)
}
