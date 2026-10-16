package com.akshar.dyslexiareader.domain.repository

import com.akshar.dyslexiareader.data.local.entity.TeacherNoteEntity
import com.akshar.dyslexiareader.domain.model.StudentProgressStats
import kotlinx.coroutines.flow.Flow

interface StatsRepository {

    fun getStudentProgress(profileId: String): Flow<StudentProgressStats?>

    suspend fun getStudentProgressDirect(profileId: String): StudentProgressStats?

    fun getTeacherNotes(profileId: String): Flow<List<TeacherNoteEntity>>

    suspend fun addTeacherNote(
        profileId: String,
        noteText: String,
        category: String = "General",
        authorName: String = "Teacher"
    )

    suspend fun deleteTeacherNote(noteId: String)

    suspend fun deleteStudentStats(profileId: String)
}
