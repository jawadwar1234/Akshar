package com.akshar.dyslexiareader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.akshar.dyslexiareader.data.local.dao.ChildProfileDao
import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.dao.PageDao
import com.akshar.dyslexiareader.data.local.dao.ReaderSettingsDao
import com.akshar.dyslexiareader.data.local.dao.ReadingSessionDao
import com.akshar.dyslexiareader.data.local.dao.WordMasteryDao
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.local.entity.PageEntity
import com.akshar.dyslexiareader.data.local.entity.ReaderSettingsEntity
import com.akshar.dyslexiareader.data.local.entity.ReadingSessionEntity
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity

@Database(
    entities = [
        DocumentEntity::class,
        ChildProfileEntity::class,
        WordMasteryEntity::class,
        PageEntity::class,
        ReaderSettingsEntity::class,
        ReadingSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun childProfileDao(): ChildProfileDao
    abstract fun wordMasteryDao(): WordMasteryDao
    abstract fun pageDao(): PageDao
    abstract fun readerSettingsDao(): ReaderSettingsDao
    abstract fun readingSessionDao(): ReadingSessionDao

    companion object {
        const val DATABASE_NAME = "akshar_dyslexia_reader.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add columns to documents if upgrading from v1
                db.execSQL("ALTER TABLE documents ADD COLUMN subject TEXT NOT NULL DEFAULT 'General'")
                db.execSQL("ALTER TABLE documents ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE documents ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE documents ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

                // Add isDeleted to child_profiles and word_mastery
                db.execSQL("ALTER TABLE child_profiles ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE word_mastery ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE word_mastery ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE word_mastery ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

                // Create new tables: pages, reader_settings, reading_sessions
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pages (
                        id TEXT PRIMARY KEY NOT NULL,
                        documentId TEXT NOT NULL,
                        pageNumber INTEGER NOT NULL,
                        imagePath TEXT,
                        rawOcrText TEXT NOT NULL,
                        cleanText TEXT NOT NULL,
                        detectedScript TEXT NOT NULL,
                        wordCount INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        isDeleted INTEGER NOT NULL
                    )
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reader_settings (
                        id TEXT PRIMARY KEY NOT NULL,
                        childProfileId TEXT NOT NULL,
                        theme TEXT NOT NULL,
                        fontSize REAL NOT NULL,
                        lineSpacing REAL NOT NULL,
                        letterSpacing REAL NOT NULL,
                        wordSpacing REAL NOT NULL,
                        marginWidth INTEGER NOT NULL,
                        fontFamily TEXT NOT NULL,
                        rulerEnabled INTEGER NOT NULL,
                        rulerWindowLines INTEGER NOT NULL,
                        bionicEnabled INTEGER NOT NULL,
                        syllablesEnabled INTEGER NOT NULL,
                        chunkingPaletteIndex INTEGER NOT NULL,
                        ttsSpeed REAL NOT NULL,
                        ttsPitch REAL NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        isDeleted INTEGER NOT NULL
                    )
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reading_sessions (
                        id TEXT PRIMARY KEY NOT NULL,
                        childProfileId TEXT NOT NULL,
                        documentId TEXT NOT NULL,
                        durationSeconds INTEGER NOT NULL,
                        wordsReadCount INTEGER NOT NULL,
                        startProgressPercent INTEGER NOT NULL,
                        endProgressPercent INTEGER NOT NULL,
                        lastReadWordIndex INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        isDeleted INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}
