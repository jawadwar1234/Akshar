package com.akshar.dyslexiareader.di

import android.content.Context
import androidx.room.Room
import com.akshar.dyslexiareader.data.local.AppDatabase
import com.akshar.dyslexiareader.data.local.dao.ChildProfileDao
import com.akshar.dyslexiareader.data.local.dao.DocumentDao
import com.akshar.dyslexiareader.data.local.dao.PageDao
import com.akshar.dyslexiareader.data.local.dao.ReaderSettingsDao
import com.akshar.dyslexiareader.data.local.dao.ReadingSessionDao
import com.akshar.dyslexiareader.data.local.dao.WordMasteryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun provideChildProfileDao(database: AppDatabase): ChildProfileDao {
        return database.childProfileDao()
    }

    @Provides
    fun provideWordMasteryDao(database: AppDatabase): WordMasteryDao {
        return database.wordMasteryDao()
    }

    @Provides
    fun providePageDao(database: AppDatabase): PageDao {
        return database.pageDao()
    }

    @Provides
    fun provideReaderSettingsDao(database: AppDatabase): ReaderSettingsDao {
        return database.readerSettingsDao()
    }

    @Provides
    fun provideReadingSessionDao(database: AppDatabase): ReadingSessionDao {
        return database.readingSessionDao()
    }

    @Provides
    fun provideTeacherNoteDao(database: AppDatabase): com.akshar.dyslexiareader.data.local.dao.TeacherNoteDao {
        return database.teacherNoteDao()
    }
}
