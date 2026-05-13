package com.yunshangguizhou.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wear_record")
data class WearRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clothingId: Long,
    val date: String,
    val wasWorn: Boolean = true
)
