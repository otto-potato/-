package com.yunshangguizhou.app.data.remote

data class QWeatherGeoResponse(
    val code: String? = null,
    val location: List<QWeatherLocation>? = null
)

data class QWeatherLocation(
    val id: String? = null,
    val name: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    val adm2: String? = null,
    val adm1: String? = null,
    val country: String? = null
)

data class QWeatherDailyResponse(
    val code: String? = null,
    val daily: List<QWeatherDaily>? = null,
    val updateTime: String? = null
)

data class QWeatherDaily(
    val fxDate: String? = null,
    val tempMax: String? = null,
    val tempMin: String? = null,
    val textDay: String? = null,
    val textNight: String? = null,
    val windDirDay: String? = null,
    val windSpeedDay: String? = null,
    val humidity: String? = null,
    val precip: String? = null,
    val uvIndex: String? = null
)

enum class WeatherSource {
    QWEATHER, OPEN_METEO
}
