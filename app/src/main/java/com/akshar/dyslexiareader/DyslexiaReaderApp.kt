package com.akshar.dyslexiareader

import android.app.Application
import com.akshar.dyslexiareader.data.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DyslexiaReaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            SyncWorker.schedulePeriodicSync(this)
            SyncWorker.triggerImmediateSync(this)
        } catch (_: Exception) {
            // Safe fallback in test environments where WorkManager is mocked
        }
    }
}
