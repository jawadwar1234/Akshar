package com.akshar.dyslexiareader.di

import com.akshar.dyslexiareader.data.ocr.CompositeOcrEngine
import com.akshar.dyslexiareader.domain.ocr.OcrEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OcrModule {

    @Binds
    @Singleton
    abstract fun bindOcrEngine(
        impl: CompositeOcrEngine
    ): OcrEngine
}
