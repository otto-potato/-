package com.yunshangguizhou.app.data.remote

import com.yunshangguizhou.app.data.repository.SettingsRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object QWeatherClient {
    private const val GEO_BASE_URL = "https://geoapi.qweather.com/"
    private const val WEATHER_BASE_URL = "https://devapi.qweather.com/"

    private val geoApi: QWeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QWeatherApiService::class.java)
    }

    private val weatherApi: QWeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QWeatherApiService::class.java)
    }

    suspend fun lookupCity(cityName: String, apiKey: String): Result<String> {
        return try {
            val response = geoApi.cityLookup(cityName, apiKey)
            if (response.code == "200" && !response.location.isNullOrEmpty()) {
                Result.success(response.location[0].id ?: "")
            } else {
                Result.failure(Exception("城市查询失败: code=${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeather(locationId: String, apiKey: String): Result<WeatherInfo> {
        return try {
            val response = weatherApi.weather3d(locationId, apiKey)
            if (response.code == "200" && !response.daily.isNullOrEmpty()) {
                val today = response.daily[0]
                val weatherInfo = WeatherInfo(
                    date = today.fxDate ?: "",
                    maxTemp = today.tempMax?.toDoubleOrNull() ?: 0.0,
                    minTemp = today.tempMin?.toDoubleOrNull() ?: 0.0,
                    weatherCode = 0,
                    weatherDesc = today.textDay ?: "未知",
                    windSpeed = today.windSpeedDay?.toDoubleOrNull() ?: 0.0,
                    humidity = today.humidity?.toIntOrNull() ?: 0
                )
                Result.success(weatherInfo)
            } else {
                Result.failure(Exception("天气查询失败: code=${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
