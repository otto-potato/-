package com.yunshangguizhou.app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenMeteoClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    private val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    suspend fun getWeather(latitude: Double, longitude: Double): Result<WeatherInfo> {
        return try {
            val response = api.getWeather(latitude = latitude, longitude = longitude)
            val daily = response.daily
            if (daily != null && !daily.time.isNullOrEmpty()) {
                val weatherInfo = WeatherInfo(
                    date = daily.time[0],
                    maxTemp = daily.temperature_2m_max?.getOrNull(0) ?: 0.0,
                    minTemp = daily.temperature_2m_min?.getOrNull(0) ?: 0.0,
                    weatherCode = daily.weathercode?.getOrNull(0) ?: 0,
                    weatherDesc = WeatherCodeMapper.getDescription(daily.weathercode?.getOrNull(0) ?: 0),
                    windSpeed = daily.windspeed_10m_max?.getOrNull(0) ?: 0.0,
                    humidity = daily.relativehumidity_2m_max?.getOrNull(0) ?: 0
                )
                Result.success(weatherInfo)
            } else {
                Result.failure(Exception("无法获取天气数据"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
