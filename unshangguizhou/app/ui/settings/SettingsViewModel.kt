package com.yunshangguizhou.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunshangguizhou.app.YunShangGuiZhouApp
import com.yunshangguizhou.app.data.remote.QWeatherClient
import com.yunshangguizhou.app.data.remote.WeatherSource
import com.yunshangguizhou.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val aiUrl: String = "", val aiKey: String = "", val aiModel: String = "",
    val wxSrc: WeatherSource = WeatherSource.OPEN_METEO,
    val qwKey: String = "", val qwLocId: String = "", val qwCity: String = "北京",
    val qwSearch: String = "", val qwSearching: Boolean = false,
    val lat: String = "39.9042", val lon: String = "116.4074",
    val calendar: Boolean = true, val saving: Boolean = false, val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsVM @Inject constructor(private val repo: SettingsRepository) : ViewModel() {
    private val _s = MutableStateFlow(SettingsState()); val state: StateFlow<SettingsState> = _s.asStateFlow()
    init { viewModelScope.launch { repo.settings.collect { cfg -> _s.update { it.copy(aiUrl = cfg.aiApiUrl, aiKey = cfg.aiApiKey, aiModel = cfg.aiModelName, wxSrc = cfg.weatherSource, qwKey = cfg.qweatherKey, qwLocId = cfg.qweatherLocationId, qwCity = cfg.qweatherCityName, lat = cfg.weatherLatitude.toString(), lon = cfg.weatherLongitude.toString(), calendar = cfg.calendarEnabled) } } } }

    fun setAiUrl(v: String) { _s.update { it.copy(aiUrl = v) } }
    fun setAiKey(v: String) { _s.update { it.copy(aiKey = v) } }
    fun setAiModel(v: String) { _s.update { it.copy(aiModel = v) } }
    fun setWxSrc(v: WeatherSource) { _s.update { it.copy(wxSrc = v) } }
    fun setQwKey(v: String) { _s.update { it.copy(qwKey = v) } }
    fun setQwLoc(v: String) { _s.update { it.copy(qwLocId = v) } }
    fun setQwCity(v: String) { _s.update { it.copy(qwCity = v) } }
    fun setQwSearch(v: String) { _s.update { it.copy(qwSearch = v) } }
    fun setLat(v: String) { _s.update { it.copy(lat = v) } }
    fun setLon(v: String) { _s.update { it.copy(lon = v) } }
    fun setCal(v: Boolean) { _s.update { it.copy(calendar = v) } }
    fun clearSaved() { _s.update { it.copy(saved = false) } }

    fun searchCity() {
        val city = _s.value.qwSearch.ifBlank { return }
        val key = _s.value.qwKey.ifBlank { _s.update { it.copy(error = "请先输入和风天气API密钥") }; return }
        viewModelScope.launch {
            _s.update { it.copy(qwSearching = true, error = null) }
            QWeatherClient.lookupCity(city, key).fold(
                onSuccess = { id -> _s.update { it.copy(qwSearching = false, qwLocId = id, qwCity = city) } },
                onFailure = { e -> _s.update { it.copy(qwSearching = false, error = e.message) } }
            )
        }
    }

    fun save(app: YunShangGuiZhouApp) {
        val s = _s.value
        viewModelScope.launch {
            _s.update { it.copy(saving = true, error = null, saved = false) }
            try {
                repo.updateAiConfig(s.aiUrl, s.aiKey, s.aiModel)
                repo.updateWeatherSource(s.wxSrc)
                if (s.wxSrc == WeatherSource.QWEATHER) repo.updateQWeatherConfig(s.qwKey, s.qwLocId, s.qwCity)
                else repo.updateWeatherLocation(s.lat.toDoubleOrNull() ?: 39.9042, s.lon.toDoubleOrNull() ?: 116.4074)
                repo.setCalendarEnabled(s.calendar)
                app.rescheduleIfNeeded()
                _s.update { it.copy(saving = false, saved = true) }
            } catch (e: Exception) { _s.update { it.copy(saving = false, error = e.message) } }
        }
    }
}
