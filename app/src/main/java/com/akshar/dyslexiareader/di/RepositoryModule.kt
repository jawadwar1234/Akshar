package com.akshar.dyslexiareader.di

import com.akshar.dyslexiareader.data.repository.ChildProfileRepositoryImpl
import com.akshar.dyslexiareader.data.repository.DocumentRepositoryImpl
import com.akshar.dyslexiareader.data.repository.WordMasteryRepositoryImpl
import com.akshar.dyslexiareader.domain.repository.ChildProfileRepository
import com.akshar.dyslexiareader.domain.repository.DocumentRepository
import com.akshar.dyslexiareader.domain.repository.WordMasteryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        impl: DocumentRepositoryImpl
    ): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindChildProfileRepository(
        impl: ChildProfileRepositoryImpl
    ): ChildProfileRepository

    @Binds
    @Singleton
    abstract fun bindWordMasteryRepository(
        impl: WordMasteryRepositoryImpl
    ): WordMasteryRepository
}
