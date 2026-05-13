package com.yunshangguizhou.app.data.repository

import com.yunshangguizhou.app.data.local.dao.ClothingDao
import com.yunshangguizhou.app.data.local.dao.RecommendationDao
import com.yunshangguizhou.app.data.local.dao.WearRecordDao
import com.yunshangguizhou.app.data.local.entity.RecommendationEntity
import com.yunshangguizhou.app.data.local.entity.WearRecordEntity
import com.yunshangguizhou.app.data.remote.AiClient
import com.yunshangguizhou.app.data.remote.WeatherClient
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationRepository @Inject constructor(
    private val recDao: RecommendationDao,
    private val clothingDao: ClothingDao,
    private val wearDao: WearRecordDao,
    private val aiClient: AiClient,
    private val settingsRepo: SettingsRepository
) {
    fun getAll(): Flow<List<RecommendationEntity>> = recDao.getAllRecommendations()

    suspend fun getToday(): RecommendationEntity? {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return recDao.getRecommendationByDate(today)
    }

    suspend fun getRecent(limit: Int = 30): List<RecommendationEntity> = recDao.getRecentRecommendations(limit)

    suspend fun generateDaily(): Result<RecommendationEntity> {
        val settings = settingsRepo.getSettingsOnce()
        if (settings.aiApiUrl.isBlank() || settings.aiApiKey.isBlank())
            return Result.failure(Exception("请先配置AI模型"))

        val weatherResult = WeatherClient.getTodayWeather(settings)
        val weather = weatherResult.getOrElse { return Result.failure(it) }

        val clothes = clothingDao.getAvailableClothing()
        if (clothes.isEmpty()) return Result.failure(Exception("衣柜为空，请先添加衣物"))

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val data = clothes.map { c ->
            mapOf<String, Any>(
                "name" to c.name, "category" to c.category, "color" to c.color,
                "material" to c.material, "thickness" to c.thickness,
                "season" to c.season, "style" to c.style,
                "consecutiveWearDays" to c.consecutiveWearDays.toString()
            )
        }

        val aiResult = aiClient.generateOutfitRecommendation(
            settings.aiApiUrl, settings.aiApiKey, settings.aiModelName, weather, data)

        return aiResult.fold(
            onSuccess = { outfit ->
                fun find(n: String?) = clothes.find { it.name == n }
                val top = find(outfit.topName); val bottom = find(outfit.bottomName)
                val outer = find(outfit.outerwearName); val shoes = find(outfit.shoesName)
                val acc = find(outfit.accessoryName)

                val rec = RecommendationEntity(
                    date = today, topId = top?.id, bottomId = bottom?.id,
                    outerwearId = outer?.id, shoesId = shoes?.id, accessoryId = acc?.id,
                    weatherDesc = weather.weatherDesc,
                    temperature = "${weather.minTemp.toInt()}~${weather.maxTemp.toInt()}C",
                    reasoning = outfit.reasoning
                )
                recDao.insert(rec)

                listOfNotNull(top, bottom, outer, shoes, acc).forEach { c ->
                    wearDao.insert(WearRecordEntity(clothingId = c.id, date = today))
                    clothingDao.updateWearInfo(c.id, c.consecutiveWearDays + 1, System.currentTimeMillis())
                }
                Result.success(rec)
            },
            onFailure = { Result.failure(it) }
        )
    }
}
