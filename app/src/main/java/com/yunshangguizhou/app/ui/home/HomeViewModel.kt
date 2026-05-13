package com.yunshangguizhou.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.data.local.entity.RecommendationEntity
import com.yunshangguizhou.app.data.remote.WeatherClient
import com.yunshangguizhou.app.data.remote.WeatherInfo
import com.yunshangguizhou.app.data.repository.ClothingRepository
import com.yunshangguizhou.app.data.repository.RecommendationRepository
import com.yunshangguizhou.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val todayRecommendation: RecommendationEntity? = null,
    val recommendedClothes: List<ClothingEntity> = emptyList(),
    val todayWeather: WeatherInfo? = null,
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val weatherError: Boolean = false,
    val error: String? = null,
    val clothingCount: Int = 0,
    val aiConfigured: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val clothingRepository: ClothingRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadTodayData() }

    private fun loadTodayData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val settings = settingsRepository.getSettingsOnce()
                val aiConfigured = settings.aiApiUrl.isNotBlank() && settings.aiApiKey.isNotBlank()

                val recommendation = recommendationRepository.getTodayRecommendation()
                val clothes = if (recommendation != null) loadRecommendationClothes(recommendation) else emptyList()
                val count = clothingRepository.getCount()

                _uiState.update {
                    it.copy(
                        todayRecommendation = recommendation,
                        recommendedClothes = clothes,
                        clothingCount = count,
                        aiConfigured = aiConfigured,
                        isLoading = false
                    )
                }

                try {
                    val weatherResult = WeatherClient.getTodayWeather(settings)
                    weatherResult.onSuccess { w -> _uiState.update { it.copy(todayWeather = w) } }
                        .onFailure { _uiState.update { it.copy(weatherError = true) } }
                } catch (_: Exception) {
                    _uiState.update { it.copy(weatherError = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun generateRecommendation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            try {
                val result = recommendationRepository.generateDailyRecommendation()
                result.fold(
                    onSuccess = { rec ->
                        val clothes = loadRecommendationClothes(rec)
                        _uiState.update { it.copy(todayRecommendation = rec, recommendedClothes = clothes, isGenerating = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isGenerating = false, error = e.message) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, error = e.message) }
            }
        }
    }

    fun refresh() { loadTodayData() }

    fun markClothingWorn(clothingId: Long) {
        viewModelScope.launch {
            val c = clothingRepository.getClothingById(clothingId) ?: return@launch
            clothingRepository.markWorn(clothingId, c.consecutiveWearDays + 1, System.currentTimeMillis())
        }
    }

    fun markClothingWashing(clothingId: Long, isWashing: Boolean) {
        viewModelScope.launch { clothingRepository.markWashing(clothingId, isWashing) }
    }

    private suspend fun loadRecommendationClothes(recommendation: RecommendationEntity): List<ClothingEntity> {
        return listOfNotNull(
            recommendation.topId?.let { clothingRepository.getClothingById(it) },
            recommendation.bottomId?.let { clothingRepository.getClothingById(it) },
            recommendation.outerwearId?.let { clothingRepository.getClothingById(it) },
            recommendation.shoesId?.let { clothingRepository.getClothingById(it) },
            recommendation.accessoryId?.let { clothingRepository.getClothingById(it) }
        )
    }
}
