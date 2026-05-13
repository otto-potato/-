package com.yunshangguizhou.app.data.local.dao

import androidx.room.*
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {

    @Query("SELECT * FROM clothing ORDER BY createdAt DESC")
    fun getAllClothing(): Flow<List<ClothingEntity>>

    @Query("SELECT * FROM clothing WHERE id = :id")
    suspend fun getClothingById(id: Long): ClothingEntity?

    @Query("SELECT * FROM clothing WHERE category = :category ORDER BY createdAt DESC")
    fun getClothingByCategory(category: String): Flow<List<ClothingEntity>>

    @Query("SELECT * FROM clothing WHERE isWashing = 0 ORDER BY createdAt DESC")
    suspend fun getAvailableClothing(): List<ClothingEntity>

    @Query("SELECT * FROM clothing WHERE category = :category AND isWashing = 0")
    suspend fun getAvailableByCategory(category: String): List<ClothingEntity>

    @Insert
    suspend fun insert(clothing: ClothingEntity): Long

    @Update
    suspend fun update(clothing: ClothingEntity)

    @Delete
    suspend fun delete(clothing: ClothingEntity)

    @Query("UPDATE clothing SET isWashing = :isWashing WHERE id = :id")
    suspend fun updateWashingStatus(id: Long, isWashing: Boolean)

    @Query("UPDATE clothing SET consecutiveWearDays = :days, lastWornDate = :date WHERE id = :id")
    suspend fun updateWearInfo(id: Long, days: Int, date: Long?)

    @Query("UPDATE clothing SET consecutiveWearDays = 0 WHERE id = :id")
    suspend fun resetWearCount(id: Long)

    @Query("SELECT COUNT(*) FROM clothing")
    suspend fun getCount(): Int
}
