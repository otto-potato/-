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
    val recommendation: RecommendationEntity? = null,
    val recClothes: List<ClothingEntity> = emptyList(),
    val weather: WeatherInfo? = null,
    val loading: Boolean = true,
    val generating: Boolean = false,
    val weatherErr: Boolean = false,
    val error: String? = null,
    val count: Int = 0,
    val aiReady: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recRepo: RecommendationRepository,
    private val clothRepo: ClothingRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _s = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _s.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _s.update { it.copy(loading = true) }
            try {
                val cfg = settingsRepo.getSettingsOnce()
                val rec = recRepo.getToday()
                val clothes = rec?.let { loadClothes(it) } ?: emptyList()
                val count = clothRepo.getCount()
                _s.update { it.copy(recommendation = rec, recClothes = clothes, count = count,
                    aiReady = cfg.aiApiUrl.isNotBlank() && cfg.aiApiKey.isNotBlank(), loading = false) }
                try {
                    WeatherClient.getTodayWeather(cfg).onSuccess { w -> _s.update { it.copy(weather = w) } }
                        .onFailure { _s.update { it.copy(weatherErr = true) } }
                } catch (_: Exception) { _s.update { it.copy(weatherErr = true) } }
            } catch (e: Exception) { _s.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun generate() {
        viewModelScope.launch {
            _s.update { it.copy(generating = true, error = null) }
            recRepo.generateDaily().fold(
                onSuccess = { r ->
                    _s.update { it.copy(recommendation = r, recClothes = loadClothes(r), generating = false) }
                },
                onFailure = { e -> _s.update { it.copy(generating = false, error = e.message) } }
            )
        }
    }

    fun refresh() { load() }

    fun markWorn(id: Long) {
        viewModelScope.launch {
            clothRepo.getById(id)?.let { clothRepo.markWorn(id, it.consecutiveWearDays + 1, System.currentTimeMillis()) }
        }
    }

    fun toggleWash(id: Long, washing: Boolean) {
        viewModelScope.launch { clothRepo.toggleWash(id, washing) }
    }

    private suspend fun loadClothes(rec: RecommendationEntity): List<ClothingEntity> = listOfNotNull(
        rec.topId?.let { clothRepo.getById(it) }, rec.bottomId?.let { clothRepo.getById(it) },
        rec.outerwearId?.let { clothRepo.getById(it) }, rec.shoesId?.let { clothRepo.getById(it) },
        rec.accessoryId?.let { clothRepo.getById(it) }
    )
}
