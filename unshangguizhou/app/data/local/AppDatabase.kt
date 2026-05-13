package com.yunshangguizhou.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yunshangguizhou.app.data.local.dao.ClothingDao
import com.yunshangguizhou.app.data.local.dao.RecommendationDao
import com.yunshangguizhou.app.data.local.dao.WearRecordDao
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.data.local.entity.RecommendationEntity
import com.yunshangguizhou.app.data.local.entity.WearRecordEntity

@Database(
    entities = [
        ClothingEntity::class,
        RecommendationEntity::class,
        WearRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun wearRecordDao(): WearRecordDao
}
