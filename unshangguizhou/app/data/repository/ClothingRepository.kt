package com.yunshangguizhou.app.data.repository

import com.yunshangguizhou.app.data.local.dao.ClothingDao
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.data.remote.AiClient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClothingRepository @Inject constructor(
    private val dao: ClothingDao,
    private val aiClient: AiClient
) {
    fun getAll(): Flow<List<ClothingEntity>> = dao.getAllClothing()
    fun getByCategory(c: String): Flow<List<ClothingEntity>> = dao.getClothingByCategory(c)
    suspend fun getById(id: Long): ClothingEntity? = dao.getClothingById(id)
    suspend fun getAvailable(): List<ClothingEntity> = dao.getAvailableClothing()
    suspend fun getByCategoryAvail(c: String): List<ClothingEntity> = dao.getAvailableByCategory(c)
    suspend fun getCount(): Int = dao.getCount()

    suspend fun addWithAi(
        name: String, category: String, color: String, material: String,
        thickness: String, season: String, style: String, imageUri: String?,
        aiUrl: String, aiKey: String, modelName: String
    ): Result<Pair<Long, Boolean>> {
        val r = aiClient.analyzeClothing(aiUrl, aiKey, modelName, name, category, color, material, thickness, season, style)
        return r.fold(
            onSuccess = { analysis ->
                val e = ClothingEntity(
                    name = analysis.name.ifEmpty { name },
                    category = analysis.category.ifEmpty { category },
                    color = analysis.color.ifEmpty { color },
                    material = analysis.material.ifEmpty { material },
                    thickness = analysis.thickness.ifEmpty { thickness },
                    season = analysis.season.ifEmpty { season },
                    style = analysis.style.ifEmpty { style },
                    description = analysis.description.ifEmpty { name },
                    imageUri = imageUri
                )
                val id = dao.insert(e)
                Result.success(id to true)
            },
            onFailure = { error ->
                val e = ClothingEntity(
                    name = name, category = category, color = color,
                    material = material, thickness = thickness,
                    season = season, style = style, description = name, imageUri = imageUri
                )
                val id = dao.insert(e)
                Result.success(id to false)
            }
        )
    }

    suspend fun addManual(
        name: String, category: String, color: String, material: String,
        thickness: String, season: String, style: String, description: String, imageUri: String?
    ): Long {
        return dao.insert(ClothingEntity(
            name = name, category = category, color = color, material = material,
            thickness = thickness, season = season, style = style,
            description = description, imageUri = imageUri))
    }

    suspend fun update(c: ClothingEntity) = dao.update(c)
    suspend fun delete(c: ClothingEntity) = dao.delete(c)
    suspend fun toggleWash(id: Long, washing: Boolean) = dao.updateWashingStatus(id, washing)
    suspend fun markWorn(id: Long, days: Int, date: Long?) = dao.updateWearInfo(id, days, date)
    suspend fun resetWear(id: Long) = dao.resetWearCount(id)
}
