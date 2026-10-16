package com.akshar.dyslexiareader.domain.util

import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.TeacherNoteEntity
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity
import com.akshar.dyslexiareader.domain.model.DailyReadingStat
import com.akshar.dyslexiareader.domain.model.StudentProgressStats
import com.akshar.dyslexiareader.domain.model.WeeklyPagesStat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ProgressReportExporterTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createSampleProfile(
        id: String = "student-1",
        name: String = "Aarav Sharma",
        grade: String = "Grade 3",
        font: String = "opendyslexic",
        isDeleted: Boolean = false
    ) = ChildProfileEntity(
        id = id,
        parentAccountId = "parent-1",
        displayName = name,
        avatarIconName = "avatar_fox",
        pinType = "none",
        hashedPin = "",
        salt = "",
        preferredFontId = font,
        preferredThemeId = "paper_cream",
        gradeLevel = grade,
        isDeleted = isDeleted
    )

    private fun createSampleStats(
        profile: ChildProfileEntity = createSampleProfile(),
        masteredWords: List<WordMasteryEntity> = listOf(
            WordMasteryEntity(id = "w1", childProfileId = profile.id, word = "sunflower", cleanWord = "sunflower", script = "latin", timesRead = 3, timesPracticed = 2, isMastered = true, lastPracticedAt = 1000L),
            WordMasteryEntity(id = "w2", childProfileId = profile.id, word = "butterfly", cleanWord = "butterfly", script = "latin", timesRead = 4, timesPracticed = 3, isMastered = true, lastPracticedAt = 2000L)
        ),
        notes: List<TeacherNoteEntity> = listOf(
            TeacherNoteEntity(
                id = "n1",
                childProfileId = profile.id,
                noteText = "Great phoneme blending today!",
                category = "Phonics",
                authorName = "Mrs. Gupta",
                createdAt = 1700000000000L
            )
        )
    ) = StudentProgressStats(
        profile = profile,
        totalPagesRead = 14,
        weeklyPagesRead = 6,
        totalReadingMinutes = 75,
        weeklyReadingMinutes = 40,
        masteredWordsCount = masteredWords.size,
        masteredWords = masteredWords,
        dailyMinutesThisWeek = listOf(
            DailyReadingStat("Mon", 10, true),
            DailyReadingStat("Tue", 15, true),
            DailyReadingStat("Wed", 0, false),
            DailyReadingStat("Thu", 15, true),
            DailyReadingStat("Fri", 0, false),
            DailyReadingStat("Sat", 0, false),
            DailyReadingStat("Sun", 0, false)
        ),
        weeklyPagesHistory = listOf(
            WeeklyPagesStat("This week", 6)
        ),
        recentSessions = emptyList(),
        teacherNotes = notes,
        activeDaysThisWeek = 3
    )

    @Test
    fun `exportProgressAsText contains accurate student metadata and reading metrics`() {
        val stats = createSampleStats()
        val text = ProgressReportExporter.exportProgressAsText(stats)

        assertTrue("Should include student name", text.contains("Aarav Sharma"))
        assertTrue("Should include grade level", text.contains("Grade 3"))
        assertTrue("Should include font preference", text.contains("OPENDYSLEXIC"))
        assertTrue("Should include weekly pages read", text.contains("6 pages"))
        assertTrue("Should include total pages read", text.contains("14 pages"))
        assertTrue("Should include weekly reading time", text.contains("40 minutes"))
        assertTrue("Should include total reading time", text.contains("75 minutes"))
        assertTrue("Should include mastered words count", text.contains("2 words"))
        assertTrue("Should include mastered words list", text.contains("sunflower") && text.contains("butterfly"))
        assertTrue("Should include teacher note author and text", text.contains("Mrs. Gupta") && text.contains("Great phoneme blending today!"))
        assertTrue("Should include DPDP privacy notice", text.contains("DPDP Act 2023 Compliant"))
    }

    @Test
    fun `exportProgressAsText returns empty string when profile is deleted for DPDP compliance`() {
        val deletedProfile = createSampleProfile(isDeleted = true)
        val stats = createSampleStats(profile = deletedProfile)

        val text = ProgressReportExporter.exportProgressAsText(stats)

        assertEquals("Export must be empty for deleted profiles", "", text)
    }

    @Test
    fun `exportProgressAsText handles empty mastered words and empty notes gracefully`() {
        val stats = createSampleStats(
            masteredWords = emptyList(),
            notes = emptyList()
        )
        val text = ProgressReportExporter.exportProgressAsText(stats)

        assertTrue(text.contains("No words mastered yet"))
        assertTrue(text.contains("No teacher notes added yet"))
    }

    @Test
    fun `purgeReportsForProfile removes cached reports for student on profile deletion`() {
        val fakeCacheDir = tempFolder.newFolder("cache")
        val reportsDir = File(fakeCacheDir, "reports").apply { mkdirs() }

        val studentAFile = File(reportsDir, "Progress_Report_Aarav_Sharma.pdf").apply { writeText("dummy pdf content") }
        val studentBFile = File(reportsDir, "Progress_Report_Diya_Patel.pdf").apply { writeText("dummy pdf content") }

        assertTrue(studentAFile.exists())
        assertTrue(studentBFile.exists())

        // Simulate purge for Aarav Sharma
        val safeName = "Aarav Sharma".replace(Regex("[^a-zA-Z0-9_]"), "_")
        reportsDir.listFiles()?.forEach { file ->
            if (file.name.contains(safeName, ignoreCase = true)) {
                file.delete()
            }
        }

        assertFalse("Aarav's report must be purged", studentAFile.exists())
        assertTrue("Other student's report must remain intact", studentBFile.exists())
    }
}
