package com.yunshangguizhou.app.di

import android.content.Context
import androidx.room.Room
import com.yunshangguizhou.app.data.local.AppDatabase
import com.yunshangguizhou.app.data.local.dao.ClothingDao
import com.yunshangguizhou.app.data.local.dao.RecommendationDao
import com.yunshangguizhou.app.data.local.dao.WearRecordDao
import com.yunshangguizhou.app.data.remote.AiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "yunshangguizhou.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideClothingDao(database: AppDatabase): ClothingDao = database.clothingDao()

    @Provides
    fun provideRecommendationDao(database: AppDatabase): RecommendationDao = database.recommendationDao()

    @Provides
    fun provideWearRecordDao(database: AppDatabase): WearRecordDao = database.wearRecordDao()

    @Provides
    @Singleton
    fun provideAiClient(): AiClient = AiClient()
}
