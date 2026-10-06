package com.akshar.dyslexiareader.data.sync

import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import org.junit.Assert.assertEquals
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
    }
}
