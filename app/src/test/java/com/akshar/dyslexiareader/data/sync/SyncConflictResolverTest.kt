package com.akshar.dyslexiareader.data.sync

import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.local.entity.ReaderSettingsEntity
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncConflictResolverTest {

    @Test
    fun resolveDocumentConflict_picksMoreRecentContentAndPreservesProgress() {
        val local = DocumentEntity(
            id = "doc1",
            childProfileId = "child1",
            title = "Chapter 1 Local Edit",
            rawOcrText = "Raw",
            cleanText = "Clean local text",
            detectedScript = "DEVANAGARI",
            wordCount = 50,
            readingProgressPercent = 80,
            lastReadWordIndex = 40,
            updatedAt = 2000L
        )

        val remote = DocumentEntity(
            id = "doc1",
            childProfileId = "child1",
            title = "Chapter 1 Remote Edit",
            rawOcrText = "Raw",
            cleanText = "Clean remote text newer",
            detectedScript = "DEVANAGARI",
            wordCount = 55,
            readingProgressPercent = 40,
            lastReadWordIndex = 20,
            updatedAt = 3000L // Remote is newer
        )

        val resolved = SyncConflictResolver.resolveDocumentConflict(local, remote)

        assertEquals("Chapter 1 Remote Edit", resolved.title)
        assertEquals("Clean remote text newer", resolved.cleanText)
        assertEquals(80, resolved.readingProgressPercent) // Highest progress preserved
        assertEquals(40, resolved.lastReadWordIndex)
        assertEquals(3000L, resolved.updatedAt)
        assertTrue(resolved.isSynced)
        assertFalse(resolved.isDeleted)
    }

    @Test
    fun resolveDocumentConflict_localNewer_keepsLocalContent() {
        val local = DocumentEntity(
            id = "doc2",
            childProfileId = "child1",
            title = "Local Newer Title",
            rawOcrText = "Raw",
            cleanText = "Local newest text",
            detectedScript = "LATIN",
            readingProgressPercent = 30,
            lastReadWordIndex = 15,
            updatedAt = 5000L
        )

        val remote = DocumentEntity(
            id = "doc2",
            childProfileId = "child1",
            title = "Old Remote Title",
            rawOcrText = "Raw",
            cleanText = "Old remote text",
            detectedScript = "LATIN",
            readingProgressPercent = 20,
            lastReadWordIndex = 10,
            updatedAt = 4000L
        )

        val resolved = SyncConflictResolver.resolveDocumentConflict(local, remote)
        assertEquals("Local Newer Title", resolved.title)
        assertEquals("Local newest text", resolved.cleanText)
        assertEquals(5000L, resolved.updatedAt)
        assertTrue(resolved.isSynced)
    }

    @Test
    fun resolveDocumentConflict_respectsRecentDeletion() {
        val local = DocumentEntity(
            id = "doc3",
            childProfileId = "child1",
            title = "Deleted Doc",
            rawOcrText = "",
            cleanText = "",
            detectedScript = "LATIN",
            isDeleted = true,
            updatedAt = 6000L
        )

        val remote = DocumentEntity(
            id = "doc3",
            childProfileId = "child1",
            title = "Live Doc",
            rawOcrText = "",
            cleanText = "Live",
            detectedScript = "LATIN",
            isDeleted = false,
            updatedAt = 5000L
        )

        val resolved = SyncConflictResolver.resolveDocumentConflict(local, remote)
        assertTrue(resolved.isDeleted)
        assertEquals(6000L, resolved.updatedAt)
    }

    @Test
    fun resolveProfileConflict_picksNewerProfile() {
        val local = ChildProfileEntity(
            id = "child_1",
            parentAccountId = "user_1",
            displayName = "Aarav Old",
            hashedPin = "hash1",
            salt = "salt1",
            updatedAt = 1000L
        )
        val remote = ChildProfileEntity(
            id = "child_1",
            parentAccountId = "user_1",
            displayName = "Aarav New",
            hashedPin = "hash2",
            salt = "salt2",
            updatedAt = 2000L
        )

        val resolved = SyncConflictResolver.resolveProfileConflict(local, remote)
        assertEquals("Aarav New", resolved.displayName)
        assertEquals("hash2", resolved.hashedPin)
        assertEquals(2000L, resolved.updatedAt)
        assertTrue(resolved.isSynced)
    }

    @Test
    fun resolveSettingsConflict_picksNewerSettings() {
        val local = ReaderSettingsEntity(
            id = "set1",
            childProfileId = "child_1",
            fontSize = 24f,
            theme = "cream",
            updatedAt = 2500L
        )
        val remote = ReaderSettingsEntity(
            id = "set1",
            childProfileId = "child_1",
            fontSize = 28f,
            theme = "sepia",
            updatedAt = 3000L
        )

        val resolved = SyncConflictResolver.resolveSettingsConflict(local, remote)
        assertEquals(28f, resolved.fontSize)
        assertEquals("sepia", resolved.theme)
        assertEquals(3000L, resolved.updatedAt)
    }

    @Test
    fun resolveWordConflict_mergesMasteryAndHighestCounts() {
        val local = WordMasteryEntity(
            id = "w1",
            childProfileId = "child_1",
            word = "butterfly",
            cleanWord = "butterfly",
            script = "LATIN",
            timesRead = 5,
            timesPracticed = 2,
            isMastered = false,
            updatedAt = 1000L
        )
        val remote = WordMasteryEntity(
            id = "w1",
            childProfileId = "child_1",
            word = "butterfly",
            cleanWord = "butterfly",
            script = "LATIN",
            timesRead = 3,
            timesPracticed = 6,
            isMastered = true,
            updatedAt = 2000L
        )

        val resolved = SyncConflictResolver.resolveWordConflict(local, remote)
        assertTrue(resolved.isMastered)
        assertEquals(5, resolved.timesRead)
        assertEquals(6, resolved.timesPracticed)
        assertEquals(2000L, resolved.updatedAt)
        assertTrue(resolved.isSynced)
    }
}
