package com.akshar.dyslexiareader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.akshar.dyslexiareader.data.local.dao.ChildProfileDao
import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.dao.WordMasteryDao
import com.akshar.dyslexiareader.data.local.entity.ChildProfileEntity
import com.akshar.dyslexiareader.data.local.entity.DocumentEntity
import com.akshar.dyslexiareader.data.local.entity.WordMasteryEntity

@Database(
    entities = [
        DocumentEntity::class,
        ChildProfileEntity::class,
        WordMasteryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun childProfileDao(): ChildProfileDao
    abstract fun wordMasteryDao(): WordMasteryDao

    companion object {
        const val DATABASE_NAME = "akshar_dyslexia_reader.db"
    }
}
