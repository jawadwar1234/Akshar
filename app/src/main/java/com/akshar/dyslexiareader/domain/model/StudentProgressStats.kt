package com.akshar.dyslexiareader.domain.model

import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.ReadingSessionEntity
import com.akshar.dyslexiareader.data.local.entity.TeacherNoteEntity
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity

data class DailyReadingStat(
    val dayOfWeek: String, // "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    val minutesRead: Int,
    val isGoalMet: Boolean = minutesRead > 0
)

data class WeeklyPagesStat(
    val weekLabel: String, // "Week 1", "Week 2", "Week 3", "This Week"
    val pagesRead: Int
)

data class StudentProgressStats(
    val profile: ChildProfileEntity,
    val totalPagesRead: Int = 0,
    val weeklyPagesRead: Int = 0,
    val totalReadingMinutes: Int = 0,
    val weeklyReadingMinutes: Int = 0,
    val masteredWordsCount: Int = 0,
    val masteredWords: List<WordMasteryEntity> = emptyList(),
    val dailyMinutesThisWeek: List<DailyReadingStat> = emptyList(),
    val weeklyPagesHistory: List<WeeklyPagesStat> = emptyList(),
    val recentSessions: List<ReadingSessionEntity> = emptyList(),
    val teacherNotes: List<TeacherNoteEntity> = emptyList(),
    val activeDaysThisWeek: Int = 0
)
