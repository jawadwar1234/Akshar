package com.akshar.dyslexiareader.data.sync

import com.akshar.dyslexiareader.data.local.entity.DocumentEntity

object SyncConflictResolver {

    /**
     * Resolves conflict between a local document version and remote cloud version.
     * Uses timestamp-based Last-Write-Wins (LWW) with reading progress preservation.
     */
    fun resolveDocumentConflict(local: DocumentEntity, remote: DocumentEntity): DocumentEntity {
        val mostRecentUpdatedAt = maxOf(local.updatedAt, remote.updatedAt)
        val highestProgress = maxOf(local.readingProgressPercent, remote.readingProgressPercent)
        val highestLastReadIndex = maxOf(local.lastReadWordIndex, remote.lastReadWordIndex)

        val winningBase = if (local.updatedAt >= remote.updatedAt) local else remote

        return winningBase.copy(
            readingProgressPercent = highestProgress,
            lastReadWordIndex = highestLastReadIndex,
            updatedAt = mostRecentUpdatedAt,
            isSynced = true
        )
    }
}
