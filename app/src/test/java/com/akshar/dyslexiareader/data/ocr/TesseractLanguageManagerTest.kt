package com.akshar.dyslexiareader.data.ocr

import android.content.Context
import android.content.res.AssetManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileNotFoundException

class TesseractLanguageManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockContext: Context
    private lateinit var mockAssets: AssetManager
    private lateinit var languageManager: TesseractLanguageManager
    private lateinit var testFilesDir: File

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockAssets = mockk(relaxed = true)
        testFilesDir = tempFolder.newFolder("files")

        every { mockContext.filesDir } returns testFilesDir
        every { mockContext.assets } returns mockAssets
        every { mockAssets.open(any()) } throws FileNotFoundException()

        languageManager = TesseractLanguageManager(mockContext)
    }

    @Test
    fun `getAvailableLanguages includes Gujarati and Bengali with Beta labels and exact sizes`() {
        val languages = languageManager.getAvailableLanguages()
        assertEquals(4, languages.size)

        val guj = languages.find { it.code == "guj" }
        assertNotNull("Gujarati should be supported", guj)
        assertTrue("Gujarati must be marked as Beta", guj!!.isBeta)
        assertEquals("1.8 MB", guj.sizeMbFormatted)

        val ben = languages.find { it.code == "ben" }
        assertNotNull("Bengali should be supported", ben)
        assertTrue("Bengali must be marked as Beta", ben!!.isBeta)
        assertEquals("1.7 MB", ben.sizeMbFormatted)

        val eng = languages.find { it.code == "eng" }
        assertNotNull("English should be supported", eng)
        assertFalse("English is not Beta", eng!!.isBeta)
        assertEquals("4.1 MB", eng.sizeMbFormatted)
    }

    @Test
    fun `downloadLanguage installs file to tessdata directory and deleteLanguage removes it`() = runTest {
        assertFalse(languageManager.isLanguageDownloaded("guj"))

        var progressReported = 0f
        val result = languageManager.downloadLanguage("guj") { p ->
            progressReported = p
        }

        assertTrue("Download should succeed", result.isSuccess)
        assertTrue("Gujarati should now be recognized as downloaded", languageManager.isLanguageDownloaded("guj"))
        assertEquals(1.0f, progressReported, 0.01f)

        val deleted = languageManager.deleteLanguage("guj")
        assertTrue("Deletion should succeed", deleted)
        assertFalse("Gujarati should no longer be downloaded", languageManager.isLanguageDownloaded("guj"))
    }
}
