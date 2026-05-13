package com.yunshangguizhou.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendation")
data class RecommendationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val topId: Long? = null,
    val bottomId: Long? = null,
    val outerwearId: Long? = null,
    val shoesId: Long? = null,
    val accessoryId: Long? = null,
    val weatherDesc: String,
    val temperature: String,
    val reasoning: String,
    val createdAt: Long = System.currentTimeMillis()
)
