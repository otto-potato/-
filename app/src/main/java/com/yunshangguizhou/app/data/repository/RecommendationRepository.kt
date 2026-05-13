package com.yunshangguizhou.app.data.repository

import com.yunshangguizhou.app.data.local.dao.ClothingDao
import com.yunshangguizhou.app.data.local.dao.RecommendationDao
import com.yunshangguizhou.app.data.local.dao.WearRecordDao
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.data.local.entity.RecommendationEntity
import com.yunshangguizhou.app.data.local.entity.WearRecordEntity
import com.yunshangguizhou.app.data.remote.AiClient
import com.yunshangguizhou.app.data.remote.WeatherClient
import com.yunshangguizhou.app.data.remote.WeatherInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationRepository @Inject constructor(
    private val recommendationDao: RecommendationDao,
    private val clothingDao: ClothingDao,
    private val wearRecordDao: WearRecordDao,
    private val aiClient: AiClient,
    private val settingsRepository: SettingsRepository
) {
    fun getAllRecommendations(): Flow<List<RecommendationEntity>> =
        recommendationDao.getAllRecommendations()

    suspend fun getTodayRecommendation(): RecommendationEntity? {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return recommendationDao.getRecommendationByDate(today)
    }

    suspend fun getRecentRecommendations(limit: Int = 7): List<RecommendationEntity> =
        recommendationDao.getRecentRecommendations(limit)

    suspend fun generateDailyRecommendation(): Result<RecommendationEntity> {
        return try {
            val settings = settingsRepository.getSettingsOnce()

            if (settings.aiApiUrl.isBlank() || settings.aiApiKey.isBlank()) {
                return Result.failure(Exception("请先在设置中配置AI模型信息"))
            }

            val weatherResult = WeatherClient.getTodayWeather(settings)

            val weatherInfo = weatherResult.getOrElse {
                return Result.failure(it)
            }

            val availableClothes = clothingDao.getAvailableClothing()
            if (availableClothes.isEmpty()) {
                return Result.failure(Exception("衣柜中没有可用的衣物，请先添加衣物"))
            }

            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val clothesData = availableClothes.map { clothing ->
                mapOf(
                    "id" to clothing.id.toString(),
                    "name" to clothing.name,
                    "category" to clothing.category,
                    "color" to clothing.color,
                    "material" to clothing.material,
                    "thickness" to clothing.thickness,
                    "season" to clothing.season,
                    "style" to clothing.style,
                    "description" to clothing.description,
                    "consecutiveWearDays" to clothing.consecutiveWearDays.toString()
                )
            }

            val aiResult = aiClient.generateOutfitRecommendation(
                apiUrl = settings.aiApiUrl,
                apiKey = settings.aiApiKey,
                modelName = settings.aiModelName,
                weatherInfo = weatherInfo,
                availableClothes = clothesData
            )

            aiResult.fold(
                onSuccess = { outfit ->
                    val top = availableClothes.find { it.name == outfit.topName }
                    val bottom = availableClothes.find { it.name == outfit.bottomName }
                    val outerwear = availableClothes.find { it.name == outfit.outerwearName }
                    val shoes = availableClothes.find { it.name == outfit.shoesName }
                    val accessory = availableClothes.find { it.name == outfit.accessoryName }

                    val recommendation = RecommendationEntity(
                        date = today,
                        topId = top?.id,
                        bottomId = bottom?.id,
                        outerwearId = outerwear?.id,
                        shoesId = shoes?.id,
                        accessoryId = accessory?.id,
                        weatherDesc = weatherInfo.weatherDesc,
                        temperature = "${weatherInfo.minTemp.toInt()}°C ~ ${weatherInfo.maxTemp.toInt()}°C",
                        reasoning = outfit.reasoning
                    )

                    val id = recommendationDao.insert(recommendation)

                    listOfNotNull(top, bottom, outerwear, shoes, accessory).forEach { clothing ->
                        wearRecordDao.insert(
                            WearRecordEntity(
                                clothingId = clothing.id,
                                date = today
                            )
                        )
                        clothingDao.updateWearInfo(
                            id = clothing.id,
                            days = clothing.consecutiveWearDays + 1,
                            date = System.currentTimeMillis()
                        )
                    }

                    Result.success(recommendation.copy(id = id))
                },
                onFailure = { e ->
                    Result.failure(e)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
