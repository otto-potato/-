package com.yunshangguizhou.app.data.remote

import com.yunshangguizhou.app.data.repository.AppSettings

object WeatherClient {

    suspend fun getTodayWeather(settings: AppSettings): Result<WeatherInfo> {
        return when (settings.weatherSource) {
            WeatherSource.QWEATHER -> {
                if (settings.qweatherKey.isBlank()) {
                    return Result.failure(Exception("请先配置和风天气API密钥"))
                }
                if (settings.qweatherLocationId.isBlank()) {
                    return Result.failure(Exception("请先配置城市ID，在设置中搜索城市获取"))
                }
                QWeatherClient.getWeather(settings.qweatherLocationId, settings.qweatherKey)
            }
            WeatherSource.OPEN_METEO -> {
                OpenMeteoClient.getWeather(settings.weatherLatitude, settings.weatherLongitude)
            }
        }
    }
}
