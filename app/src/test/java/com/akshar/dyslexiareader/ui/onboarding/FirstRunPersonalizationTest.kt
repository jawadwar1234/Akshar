package com.akshar.dyslexiareader.ui.onboarding

import com.akshar.dyslexiareader.domain.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstRunPersonalizationTest {

    @Test
    fun `default user settings have firstRunCompleted as false and optimal dyslexic presets`() {
        val defaultSettings = UserSettings()

        assertFalse("First run should initially be incomplete", defaultSettings.firstRunCompleted)
        assertFalse("Parental consent should initially be false", defaultSettings.parentalConsentGiven)
        assertEquals("Default theme should be warm cream", "cream", defaultSettings.readerTheme)
        assertEquals("Default font should be lexend", "lexend", defaultSettings.fontFamily)
        assertEquals("Default font size should be 22sp", 22f, defaultSettings.fontSize, 0.01f)
        assertEquals("Default line spacing should be generous 1.6x", 1.6f, defaultSettings.lineSpacing, 0.01f)
        assertEquals("Default letter spacing should be 0.08", 0.08f, defaultSettings.letterSpacing, 0.01f)
    }

    @Test
    fun `completing first-run personalization updates preferences and marks firstRunCompleted true`() {
        var settings = UserSettings()

        // Simulate completing tutorial with Sepia theme and OpenDyslexic font
        settings = settings.copy(
            firstRunCompleted = true,
            readerTheme = "sepia",
            fontFamily = "opendyslexic",
            fontSize = 26f,
            lineSpacing = 1.8f,
            letterSpacing = 0.10f,
            rulerEnabled = true
        )

        assertTrue("First run should now be marked complete", settings.firstRunCompleted)
        assertEquals("sepia", settings.readerTheme)
        assertEquals("opendyslexic", settings.fontFamily)
        assertEquals(26f, settings.fontSize, 0.01f)
        assertEquals(1.8f, settings.lineSpacing, 0.01f)
        assertEquals(0.10f, settings.letterSpacing, 0.01f)
        assertTrue(settings.rulerEnabled)
    }

    @Test
    fun `skipping first-run personalization marks firstRunCompleted true with recommended defaults`() {
        var settings = UserSettings()

        // Skip flow retains default warm cream and Lexend while marking firstRunCompleted = true
        settings = settings.copy(firstRunCompleted = true)

        assertTrue(settings.firstRunCompleted)
        assertEquals("cream", settings.readerTheme)
        assertEquals("lexend", settings.fontFamily)
        assertEquals(22f, settings.fontSize, 0.01f)
    }
}
