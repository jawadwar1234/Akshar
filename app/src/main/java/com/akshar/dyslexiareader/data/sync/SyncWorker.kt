package com.akshar.dyslexiareader.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.akshar.dyslexiareader.data.local.AppDatabase
import com.akshar.dyslexiareader.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val db = AppDatabase.getInstance(context)
            val preferencesRepo = UserPreferencesRepository(context)
            val userSettings = preferencesRepo.userSettingsFlow.first()
            val parentId = userSettings.parentAccountId

            if (parentId.isNullOrBlank() || parentId.startsWith("offline")) {
                return Result.success()
            }

            val syncManager = SyncManager(
                documentDao = db.documentDao(),
                childProfileDao = db.childProfileDao(),
                wordMasteryDao = db.wordMasteryDao(),
                pageDao = db.pageDao(),
                readerSettingsDao = db.readerSettingsDao(),
                readingSessionDao = db.readingSessionDao(),
                userPreferencesRepository = preferencesRepo
            )

            syncManager.syncPendingData(parentId)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME_PERIODIC = "akshar_periodic_sync"
        const val WORK_NAME_ONE_TIME = "akshar_immediate_sync"

        /**
         * Schedules periodic background sync every 15 minutes when connected to network.
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        /**
         * Enqueues an immediate background sync with exponential backoff on retry.
         */
        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }
}
