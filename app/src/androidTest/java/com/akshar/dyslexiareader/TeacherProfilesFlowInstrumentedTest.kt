package com.akshar.dyslexiareader

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.akshar.dyslexiareader.data.local.AppDatabase
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.local.entity.ReaderSettingsEntity
import com.akshar.dyslexiareader.data.local.entity.ReadingSessionEntity
import com.akshar.dyslexiareader.data.repository.ChildProfileRepositoryImpl
import com.akshar.dyslexiareader.data.repository.DocumentRepositoryImpl
import com.akshar.dyslexiareader.util.PinSecurityUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Instrumented acceptance test for Milestone 6:
 * Create a teacher with 3 students, switch between them, restart the app,
 * and confirm settings and reading history are completely isolated per student.
 */
@RunWith(AndroidJUnit4::class)
class TeacherProfilesFlowInstrumentedTest {

    private lateinit var db: AppDatabase
    private lateinit var profileRepo: ChildProfileRepositoryImpl
    private lateinit var documentRepo: DocumentRepositoryImpl
    private lateinit var appContext: Context

    @Before
    fun createDb() {
        appContext = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        profileRepo = ChildProfileRepositoryImpl(db.childProfileDao())
        documentRepo = DocumentRepositoryImpl(db.documentDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun acceptance_teacherWithThreeStudents_isolatedSettingsAndHistory() = runBlocking {
        val teacherAccountId = "teacher_sharma_001"

        // 1. Create Student 1: Aarav (Grade 3, Picture PIN: Dog -> Star -> Sun)
        val salt1 = PinSecurityUtil.generateSalt()
        val picPinAarav = PinSecurityUtil.encodePicturePin(listOf("dog", "star", "sun"))
        val student1 = ChildProfileEntity(
            id = "student_aarav_101",
            parentAccountId = teacherAccountId,
            displayName = "Aarav",
            avatarIconName = "avatar_1",
            pinType = "PICTURE",
            hashedPin = PinSecurityUtil.hashPin(picPinAarav, salt1),
            salt = salt1,
            gradeLevel = "Grade 3",
            preferredThemeId = "cream",
            preferredFontSizeSp = 24f
        )
        profileRepo.createProfile(student1)

        // 2. Create Student 2: Diya (Grade 4, Number PIN: "5678")
        val salt2 = PinSecurityUtil.generateSalt()
        val student2 = ChildProfileEntity(
            id = "student_diya_102",
            parentAccountId = teacherAccountId,
            displayName = "Diya",
            avatarIconName = "avatar_2",
            pinType = "NUMBER",
            hashedPin = PinSecurityUtil.hashPin("5678", salt2),
            salt = salt2,
            gradeLevel = "Grade 4",
            preferredThemeId = "pale_blue",
            preferredFontSizeSp = 28f
        )
        profileRepo.createProfile(student2)

        // 3. Create Student 3: Rohan (Grade 5, Picture PIN: Cat -> Apple -> Balloon)
        val salt3 = PinSecurityUtil.generateSalt()
        val picPinRohan = PinSecurityUtil.encodePicturePin(listOf("cat", "apple", "balloon"))
        val student3 = ChildProfileEntity(
            id = "student_rohan_103",
            parentAccountId = teacherAccountId,
            displayName = "Rohan",
            avatarIconName = "avatar_3",
            pinType = "PICTURE",
            hashedPin = PinSecurityUtil.hashPin(picPinRohan, salt3),
            salt = salt3,
            gradeLevel = "Grade 5",
            preferredThemeId = "sepia",
            preferredFontSizeSp = 20f
        )
        profileRepo.createProfile(student3)

        // 4. Verify all 3 students under teacher account
        val teacherProfiles = profileRepo.getProfilesForParent(teacherAccountId).first()
        assertEquals(3, teacherProfiles.size)

        // 5. Add Student 1 (Aarav) reading history & custom settings
        val aaravDoc = DocumentEntity(
            id = "doc_aarav_1",
            childProfileId = student1.id,
            title = "Aarav Science Book",
            rawOcrText = "Magnets attract iron.",
            cleanText = "Magnets attract iron.",
            detectedScript = "LATIN",
            subject = "Science",
            readingProgressPercent = 50,
            lastReadWordIndex = 2
        )
        documentRepo.saveDocument(aaravDoc)

        val aaravSettings = ReaderSettingsEntity(
            id = UUID.randomUUID().toString(),
            childProfileId = student1.id,
            theme = "cream",
            fontSize = 24f,
            rulerEnabled = true,
            rulerWindowLines = 1
        )
        db.readerSettingsDao().saveSettings(aaravSettings)

        // 6. Add Student 2 (Diya) reading history & custom settings
        val diyaDoc = DocumentEntity(
            id = "doc_diya_1",
            childProfileId = student2.id,
            title = "Diya Hindi Lesson",
            rawOcrText = "सूरज पूरब से निकलता है।",
            cleanText = "सूरज पूरब से निकलता है।",
            detectedScript = "DEVANAGARI",
            subject = "Hindi",
            readingProgressPercent = 80,
            lastReadWordIndex = 4
        )
        documentRepo.saveDocument(diyaDoc)

        val diyaSettings = ReaderSettingsEntity(
            id = UUID.randomUUID().toString(),
            childProfileId = student2.id,
            theme = "pale_blue",
            fontSize = 28f,
            rulerEnabled = false,
            syllablesEnabled = true
        )
        db.readerSettingsDao().saveSettings(diyaSettings)

        // 7. SIMULATE APP RESTART: create fresh repository references
        val freshProfileRepo = ChildProfileRepositoryImpl(db.childProfileDao())
        val freshDocumentRepo = DocumentRepositoryImpl(db.documentDao())

        // 8. VERIFY AARAV ISOLATION:
        val aaravLoadedDocs = freshDocumentRepo.getDocumentsForProfile(student1.id).first()
        assertEquals(1, aaravLoadedDocs.size)
        assertEquals("Aarav Science Book", aaravLoadedDocs[0].title)
        assertEquals(50, aaravLoadedDocs[0].readingProgressPercent)

        val aaravLoadedSettings = db.readerSettingsDao().getSettingsForProfile(student1.id).first()
        assertNotNull(aaravLoadedSettings)
        assertEquals("cream", aaravLoadedSettings?.theme)
        assertEquals(24f, aaravLoadedSettings?.fontSize)
        assertTrue(aaravLoadedSettings?.rulerEnabled == true)

        // 9. VERIFY DIYA ISOLATION:
        val diyaLoadedDocs = freshDocumentRepo.getDocumentsForProfile(student2.id).first()
        assertEquals(1, diyaLoadedDocs.size)
        assertEquals("Diya Hindi Lesson", diyaLoadedDocs[0].title)
        assertEquals(80, diyaLoadedDocs[0].readingProgressPercent)

        val diyaLoadedSettings = db.readerSettingsDao().getSettingsForProfile(student2.id).first()
        assertNotNull(diyaLoadedSettings)
        assertEquals("pale_blue", diyaLoadedSettings?.theme)
        assertEquals(28f, diyaLoadedSettings?.fontSize)
        assertTrue(diyaLoadedSettings?.syllablesEnabled == true)

        // 10. VERIFY ROHAN (Student 3) has clean empty library (no cross-profile leakage)
        val rohanLoadedDocs = freshDocumentRepo.getDocumentsForProfile(student3.id).first()
        assertEquals(0, rohanLoadedDocs.size)

        // 11. VERIFY PIN AUTHENTICATION
        // Aarav correct picture PIN succeeds
        assertTrue(PinSecurityUtil.verifyPin(picPinAarav, student1.hashedPin, student1.salt))
        // Aarav wrong picture PIN fails
        val wrongAaravPin = PinSecurityUtil.encodePicturePin(listOf("sun", "dog", "star"))
        assertFalse(PinSecurityUtil.verifyPin(wrongAaravPin, student1.hashedPin, student1.salt))

        // Diya correct 4-digit PIN succeeds
        assertTrue(PinSecurityUtil.verifyPin("5678", student2.hashedPin, student2.salt))
        // Diya wrong PIN fails
        assertFalse(PinSecurityUtil.verifyPin("1234", student2.hashedPin, student2.salt))
    }
}
