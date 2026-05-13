package com.yunshangguizhou.app.data.repository

import com.yunshangguizhou.app.data.local.dao.ClothingDao
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.data.remote.AiClient
import com.yunshangguizhou.app.data.remote.ClothingAnalysis
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClothingRepository @Inject constructor(
    private val clothingDao: ClothingDao,
    private val aiClient: AiClient
) {
    fun getAllClothing(): Flow<List<ClothingEntity>> = clothingDao.getAllClothing()

    fun getClothingByCategory(category: String): Flow<List<ClothingEntity>> =
        clothingDao.getClothingByCategory(category)

    suspend fun getClothingById(id: Long): ClothingEntity? =
        clothingDao.getClothingById(id)

    suspend fun getAvailableClothing(): List<ClothingEntity> =
        clothingDao.getAvailableClothing()

    suspend fun getAvailableByCategory(category: String): List<ClothingEntity> =
        clothingDao.getAvailableByCategory(category)

    suspend fun addClothingWithAi(
        name: String,
        category: String,
        color: String,
        material: String,
        thickness: String,
        season: String,
        style: String,
        imageUri: String?,
        aiUrl: String,
        aiKey: String,
        modelName: String
    ): Result<Long> {
        val analysisResult = aiClient.analyzeClothing(
            apiUrl = aiUrl,
            apiKey = aiKey,
            modelName = modelName,
            name = name,
            category = category,
            color = color,
            material = material,
            thickness = thickness,
            season = season,
            style = style
        )

        return analysisResult.fold(
            onSuccess = { analysis ->
                val entity = ClothingEntity(
                    name = analysis.name.ifEmpty { name },
                    category = analysis.category.ifEmpty { category },
                    color = analysis.color.ifEmpty { color },
                    material = analysis.material.ifEmpty { material },
                    thickness = analysis.thickness.ifEmpty { thickness },
                    season = analysis.season.ifEmpty { season },
                    style = analysis.style.ifEmpty { style },
                    description = analysis.description.ifEmpty { "$name" },
                    imageUri = imageUri
                )
                val id = clothingDao.insert(entity)
                Result.success(id)
            },
            onFailure = { e ->
                val entity = ClothingEntity(
                    name = name,
                    category = category,
                    color = color,
                    material = material,
                    thickness = thickness,
                    season = season,
                    style = style,
                    description = "$name",
                    imageUri = imageUri
                )
                val id = clothingDao.insert(entity)
                Result.success(id)
            }
        )
    }

    suspend fun addClothingManual(
        name: String,
        category: String,
        color: String,
        material: String,
        thickness: String,
        season: String,
        style: String,
        description: String,
        imageUri: String?
    ): Long {
        val entity = ClothingEntity(
            name = name,
            category = category,
            color = color,
            material = material,
            thickness = thickness,
            season = season,
            style = style,
            description = description,
            imageUri = imageUri
        )
        return clothingDao.insert(entity)
    }

    suspend fun updateClothing(clothing: ClothingEntity) = clothingDao.update(clothing)

    suspend fun deleteClothing(clothing: ClothingEntity) = clothingDao.delete(clothing)

    suspend fun markWashing(id: Long, isWashing: Boolean) =
        clothingDao.updateWashingStatus(id, isWashing)

    suspend fun markWorn(id: Long, days: Int, date: Long?) =
        clothingDao.updateWearInfo(id, days, date)

    suspend fun resetWearCount(id: Long) = clothingDao.resetWearCount(id)

    suspend fun getCount(): Int = clothingDao.getCount()
}
