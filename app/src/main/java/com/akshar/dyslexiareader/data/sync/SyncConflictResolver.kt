package com.akshar.dyslexiareader.data.sync

import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.local.entity.ReaderSettingsEntity
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity

object SyncConflictResolver {

    /**
     * Resolves conflict between a local document version and remote cloud version.
     * Uses timestamp-based Last-Write-Wins (LWW) with reading progress preservation.
     */
    fun resolveDocumentConflict(local: DocumentEntity, remote: DocumentEntity): DocumentEntity {
        val mostRecentUpdatedAt = maxOf(local.updatedAt, remote.updatedAt)
        val highestProgress = maxOf(local.readingProgressPercent, remote.readingProgressPercent)
        val highestLastReadIndex = maxOf(local.lastReadWordIndex, remote.lastReadWordIndex)

        // If local is deleted or remote is deleted, respect the more recent deletion state
        val isDeleted = if (local.updatedAt >= remote.updatedAt) local.isDeleted else remote.isDeleted

        val winningBase = if (local.updatedAt >= remote.updatedAt) local else remote

        return winningBase.copy(
            readingProgressPercent = highestProgress,
            lastReadWordIndex = highestLastReadIndex,
            updatedAt = mostRecentUpdatedAt,
            isDeleted = isDeleted,
            isSynced = true
        )
    }

    /**
     * Resolves conflict between local and remote child profile.
     * Uses timestamp-based Last-Write-Wins (LWW).
     */
    fun resolveProfileConflict(local: ChildProfileEntity, remote: ChildProfileEntity): ChildProfileEntity {
        val winningBase = if (local.updatedAt >= remote.updatedAt) local else remote
        return winningBase.copy(
            updatedAt = maxOf(local.updatedAt, remote.updatedAt),
            isSynced = true
        )
    }

    /**
     * Resolves conflict between local and remote reader settings.
     * Uses timestamp-based Last-Write-Wins (LWW).
     */
    fun resolveSettingsConflict(local: ReaderSettingsEntity, remote: ReaderSettingsEntity): ReaderSettingsEntity {
        val winningBase = if (local.updatedAt >= remote.updatedAt) local else remote
        return winningBase.copy(
            updatedAt = maxOf(local.updatedAt, remote.updatedAt)
        )
    }

    /**
     * Resolves conflict between local and remote practice words.
     * Merges mastery state (if mastered on either, it stays mastered) and keeps highest practice counts.
     */
    fun resolveWordConflict(local: WordMasteryEntity, remote: WordMasteryEntity): WordMasteryEntity {
        val winningBase = if (local.updatedAt >= remote.updatedAt) local else remote
        return winningBase.copy(
            isMastered = local.isMastered || remote.isMastered,
            timesRead = maxOf(local.timesRead, remote.timesRead),
            timesPracticed = maxOf(local.timesPracticed, remote.timesPracticed),
            lastPracticedAt = maxOf(local.lastPracticedAt, remote.lastPracticedAt),
            updatedAt = maxOf(local.updatedAt, remote.updatedAt),
            isSynced = true
        )
    }
}
