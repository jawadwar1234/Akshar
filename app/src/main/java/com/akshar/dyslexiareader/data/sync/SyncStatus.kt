package com.akshar.dyslexiareader.data.sync

enum class SyncStatus {
    SAVED_LOCAL,      // "Saved on this phone"
    SYNCING,          // "Syncing..."
    SAVED_TO_CLOUD,   // "Saved to your account"
    OFFLINE_PAUSED,   // "Saved on this phone (Offline)"
    ERROR             // "Sync paused"
}
