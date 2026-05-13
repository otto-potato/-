package com.yunshangguizhou.app.data.local.dao

import androidx.room.*
import com.yunshangguizhou.app.data.local.entity.WearRecordEntity

@Dao
interface WearRecordDao {

    @Query("SELECT * FROM wear_record WHERE clothingId = :clothingId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWearRecord(clothingId: Long): WearRecordEntity?

    @Query("SELECT * FROM wear_record WHERE clothingId = :clothingId AND date = :date LIMIT 1")
    suspend fun getRecordByDate(clothingId: Long, date: String): WearRecordEntity?

    @Query("SELECT COUNT(*) FROM wear_record WHERE clothingId = :clothingId AND date >= :sinceDate")
    suspend fun getWearCountSince(clothingId: Long, sinceDate: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WearRecordEntity)

    @Query("DELETE FROM wear_record WHERE date < :before")
    suspend fun deleteRecordsBefore(before: String)

    @Query("DELETE FROM wear_record WHERE clothingId = :clothingId")
    suspend fun deleteRecordsByClothing(clothingId: Long)
}
