package com.yunshangguizhou.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clothing")
data class ClothingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val color: String,
    val material: String,
    val thickness: String,
    val season: String,
    val style: String,
    val description: String,
    val imageUri: String? = null,
    val isWashing: Boolean = false,
    val consecutiveWearDays: Int = 0,
    val lastWornDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
