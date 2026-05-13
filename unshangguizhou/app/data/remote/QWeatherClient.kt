package com.yunshangguizhou.app.data.remote

import com.yunshangguizhou.app.data.repository.AppSettings
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object QWeatherClient {
    private val geo = Retrofit.Builder().baseUrl("https://geoapi.qweather.com/")
        .addConverterFactory(GsonConverterFactory.create()).build()
        .create(QWeatherApiService::class.java)
    private val wx = Retrofit.Builder().baseUrl("https://devapi.qweather.com/")
        .addConverterFactory(GsonConverterFactory.create()).build()
        .create(QWeatherApiService::class.java)

    suspend fun lookupCity(city: String, key: String): Result<String> {
        return try {
            val r = geo.cityLookup(city, key)
            if (r.code == "200" && !r.location.isNullOrEmpty()) Result.success(r.location[0].id ?: "")
            else Result.failure(Exception("城市查询失败 code:${r.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getWeather(locationId: String, key: String): Result<WeatherInfo> {
        return try {
            val r = wx.weather3d(locationId, key)
            if (r.code == "200" && !r.daily.isNullOrEmpty()) {
                val d = r.daily[0]
                Result.success(WeatherInfo(
                    date = d.fxDate ?: "", maxTemp = d.tempMax?.toDoubleOrNull() ?: 0.0,
                    minTemp = d.tempMin?.toDoubleOrNull() ?: 0.0,
                    weatherCode = qwCode(d.textDay),
                    weatherDesc = d.textDay ?: "未知",
                    windSpeed = d.windSpeedDay?.toDoubleOrNull() ?: 0.0,
                    humidity = d.humidity?.toIntOrNull() ?: 0
                ))
            } else Result.failure(Exception("天气查询失败 code:${r.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun qwCode(desc: String?): Int = when {
        desc == null -> 0
        "晴" in desc -> 0; "少云" in desc || "晴间多云" in desc -> 1
        "多云" in desc -> 2; "阴" in desc -> 3
        "雾" in desc || "霾" in desc -> 45
        "雨" in desc -> 61; "雪" in desc -> 71
        "雷" in desc -> 95; else -> 0
    }
}
