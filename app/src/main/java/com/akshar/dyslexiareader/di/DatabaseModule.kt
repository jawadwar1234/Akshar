package com.akshar.dyslexiareader.di

import android.content.Context
import androidx.room.Room
import com.akshar.dyslexiareader.data.local.AppDatabase
import com.akshar.dyslexiareader.data.local.dao.ChildProfileDao
import com.akshar.dyslexiareader.data.local.dao.DocumentDao
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
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
}
