package com.yunshangguizhou.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weathercode,windspeed_10m_max,relativehumidity_2m_max",
        @Query("timezone") timezone: String = "Asia/Shanghai",
        @Query("forecast_days") forecastDays: Int = 1
    ): WeatherResponse
}
