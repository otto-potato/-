package com.yunshangguizhou.app.data.local.dao

import androidx.room.*
import com.yunshangguizhou.app.data.local.entity.RecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationDao {

    @Query("SELECT * FROM recommendation ORDER BY date DESC")
    fun getAllRecommendations(): Flow<List<RecommendationEntity>>

    @Query("SELECT * FROM recommendation WHERE date = :date LIMIT 1")
    suspend fun getRecommendationByDate(date: String): RecommendationEntity?

    @Query("SELECT * FROM recommendation ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentRecommendations(limit: Int = 7): List<RecommendationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recommendation: RecommendationEntity): Long

    @Delete
    suspend fun delete(recommendation: RecommendationEntity)
}
