package com.yunshangguizhou.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.yunshangguizhou.app.data.remote.WeatherSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

data class AppSettings(
    val aiApiUrl: String = "",
    val aiApiKey: String = "",
    val aiModelName: String = "",
    val weatherSource: WeatherSource = WeatherSource.OPEN_METEO,
    val qweatherKey: String = "",
    val qweatherLocationId: String = "",
    val qweatherCityName: String = "北京",
    val weatherLatitude: Double = 39.9042,
    val weatherLongitude: Double = 116.4074,
    val calendarEnabled: Boolean = true
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val AI_API_URL = stringPreferencesKey("ai_api_url")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_MODEL_NAME = stringPreferencesKey("ai_model_name")
        val WEATHER_SOURCE = stringPreferencesKey("weather_source")
        val QWEATHER_KEY = stringPreferencesKey("qweather_key")
        val QWEATHER_LOCATION_ID = stringPreferencesKey("qweather_location_id")
        val QWEATHER_CITY_NAME = stringPreferencesKey("qweather_city_name")
        val WEATHER_LATITUDE = doublePreferencesKey("weather_latitude")
        val WEATHER_LONGITUDE = doublePreferencesKey("weather_longitude")
        val CALENDAR_ENABLED = booleanPreferencesKey("calendar_enabled")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            aiApiUrl = prefs[Keys.AI_API_URL] ?: "",
            aiApiKey = prefs[Keys.AI_API_KEY] ?: "",
            aiModelName = prefs[Keys.AI_MODEL_NAME] ?: "",
            weatherSource = try { WeatherSource.valueOf(prefs[Keys.WEATHER_SOURCE] ?: "OPEN_METEO") } catch (_: Exception) { WeatherSource.OPEN_METEO },
            qweatherKey = prefs[Keys.QWEATHER_KEY] ?: "",
            qweatherLocationId = prefs[Keys.QWEATHER_LOCATION_ID] ?: "",
            qweatherCityName = prefs[Keys.QWEATHER_CITY_NAME] ?: "北京",
            weatherLatitude = prefs[Keys.WEATHER_LATITUDE] ?: 39.9042,
            weatherLongitude = prefs[Keys.WEATHER_LONGITUDE] ?: 116.4074,
            calendarEnabled = prefs[Keys.CALENDAR_ENABLED] ?: true
        )
    }

    suspend fun updateAiConfig(url: String, key: String, model: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AI_API_URL] = url
            prefs[Keys.AI_API_KEY] = key
            prefs[Keys.AI_MODEL_NAME] = model
        }
    }

    suspend fun updateWeatherSource(source: WeatherSource) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WEATHER_SOURCE] = source.name
        }
    }

    suspend fun updateQWeatherConfig(key: String, locationId: String, cityName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.QWEATHER_KEY] = key
            prefs[Keys.QWEATHER_LOCATION_ID] = locationId
            prefs[Keys.QWEATHER_CITY_NAME] = cityName
        }
    }

    suspend fun updateWeatherLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WEATHER_LATITUDE] = latitude
            prefs[Keys.WEATHER_LONGITUDE] = longitude
        }
    }

    suspend fun setCalendarEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CALENDAR_ENABLED] = enabled
        }
    }

    suspend fun getSettingsOnce(): AppSettings {
        return context.dataStore.data.first().let { prefs ->
            AppSettings(
                aiApiUrl = prefs[Keys.AI_API_URL] ?: "",
                aiApiKey = prefs[Keys.AI_API_KEY] ?: "",
                aiModelName = prefs[Keys.AI_MODEL_NAME] ?: "",
                weatherSource = try { WeatherSource.valueOf(prefs[Keys.WEATHER_SOURCE] ?: "OPEN_METEO") } catch (_: Exception) { WeatherSource.OPEN_METEO },
                qweatherKey = prefs[Keys.QWEATHER_KEY] ?: "",
                qweatherLocationId = prefs[Keys.QWEATHER_LOCATION_ID] ?: "",
                qweatherCityName = prefs[Keys.QWEATHER_CITY_NAME] ?: "北京",
                weatherLatitude = prefs[Keys.WEATHER_LATITUDE] ?: 39.9042,
                weatherLongitude = prefs[Keys.WEATHER_LONGITUDE] ?: 116.4074,
                calendarEnabled = prefs[Keys.CALENDAR_ENABLED] ?: true
            )
        }
    }
}
