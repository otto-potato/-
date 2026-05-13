package com.yunshangguizhou.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunshangguizhou.app.data.remote.QWeatherClient
import com.yunshangguizhou.app.data.remote.WeatherSource
import com.yunshangguizhou.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val aiApiUrl: String = "",
    val aiApiKey: String = "",
    val aiModelName: String = "",
    val weatherSource: WeatherSource = WeatherSource.OPEN_METEO,
    val qweatherKey: String = "",
    val qweatherLocationId: String = "",
    val qweatherCityName: String = "北京",
    val qweatherCitySearch: String = "",
    val qweatherSearching: Boolean = false,
    val qweatherSearchResult: String = "",
    val weatherLatitude: String = "39.9042",
    val weatherLongitude: String = "116.4074",
    val calendarEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        aiApiUrl = settings.aiApiUrl,
                        aiApiKey = settings.aiApiKey,
                        aiModelName = settings.aiModelName,
                        weatherSource = settings.weatherSource,
                        qweatherKey = settings.qweatherKey,
                        qweatherLocationId = settings.qweatherLocationId,
                        qweatherCityName = settings.qweatherCityName,
                        weatherLatitude = settings.weatherLatitude.toString(),
                        weatherLongitude = settings.weatherLongitude.toString(),
                        calendarEnabled = settings.calendarEnabled
                    )
                }
            }
        }
    }

    fun updateAiUrl(url: String) { _uiState.update { it.copy(aiApiUrl = url) } }
    fun updateAiKey(key: String) { _uiState.update { it.copy(aiApiKey = key) } }
    fun updateModelName(name: String) { _uiState.update { it.copy(aiModelName = name) } }
    fun updateWeatherSource(source: WeatherSource) { _uiState.update { it.copy(weatherSource = source) } }
    fun updateQWeatherKey(key: String) { _uiState.update { it.copy(qweatherKey = key) } }
    fun updateQWeatherLocationId(id: String) { _uiState.update { it.copy(qweatherLocationId = id) } }
    fun updateQWeatherCityName(name: String) { _uiState.update { it.copy(qweatherCityName = name) } }
    fun updateQWeatherCitySearch(search: String) { _uiState.update { it.copy(qweatherCitySearch = search) } }
    fun updateWeatherLatitude(lat: String) { _uiState.update { it.copy(weatherLatitude = lat) } }
    fun updateWeatherLongitude(lon: String) { _uiState.update { it.copy(weatherLongitude = lon) } }
    fun updateCalendarEnabled(enabled: Boolean) { _uiState.update { it.copy(calendarEnabled = enabled) } }

    fun searchCity() {
        val city = _uiState.value.qweatherCitySearch.ifBlank { return }
        val key = _uiState.value.qweatherKey.ifBlank {
            _uiState.update { it.copy(error = "请先输入和风天气API密钥") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(qweatherSearching = true, error = null) }
            QWeatherClient.lookupCity(city, key).fold(
                onSuccess = { locationId ->
                    _uiState.update {
                        it.copy(
                            qweatherSearching = false,
                            qweatherLocationId = locationId,
                            qweatherCityName = city,
                            qweatherSearchResult = "找到城市ID: $locationId"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(qweatherSearching = false, error = e.message ?: "搜索失败")
                    }
                }
            )
        }
    }

    fun saveSettings() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            try {
                settingsRepository.updateAiConfig(state.aiApiUrl, state.aiApiKey, state.aiModelName)
                settingsRepository.updateWeatherSource(state.weatherSource)
                if (state.weatherSource == WeatherSource.QWEATHER) {
                    settingsRepository.updateQWeatherConfig(
                        state.qweatherKey, state.qweatherLocationId, state.qweatherCityName
                    )
                } else {
                    settingsRepository.updateWeatherLocation(
                        state.weatherLatitude.toDoubleOrNull() ?: 39.9042,
                        state.weatherLongitude.toDoubleOrNull() ?: 116.4074
                    )
                }
                settingsRepository.setCalendarEnabled(state.calendarEnabled)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearSaveSuccess() { _uiState.update { it.copy(saveSuccess = false) } }
}
