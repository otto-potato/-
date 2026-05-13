package com.yunshangguizhou.app.data.remote

data class WeatherResponse(
    val daily: DailyWeather? = null
) {
    data class DailyWeather(
        val time: List<String>? = null,
        val temperature_2m_max: List<Double>? = null,
        val temperature_2m_min: List<Double>? = null,
        val weathercode: List<Int>? = null,
        val windspeed_10m_max: List<Double>? = null,
        val relativehumidity_2m_max: List<Int>? = null
    )
}

data class WeatherInfo(
    val date: String,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int,
    val weatherDesc: String,
    val windSpeed: Double,
    val humidity: Int
)

object WeatherCodeMapper {
    private val codeMap = mapOf(
        0 to "晴天",
        1 to "大部晴朗",
        2 to "部分多云",
        3 to "阴天",
        45 to "雾",
        48 to "雾凇",
        51 to "小毛毛雨",
        53 to "中度毛毛雨",
        55 to "大毛毛雨",
        56 to "冻毛毛雨",
        57 to "冻毛毛雨",
        61 to "小雨",
        63 to "中雨",
        65 to "大雨",
        66 to "冻雨",
        67 to "冻雨",
        71 to "小雪",
        73 to "中雪",
        75 to "大雪",
        77 to "雪粒",
        80 to "阵雨",
        81 to "中度阵雨",
        82 to "大阵雨",
        85 to "小阵雪",
        86 to "大阵雪",
        95 to "雷暴",
        96 to "雷暴伴小冰雹",
        99 to "雷暴伴大冰雹"
    )

    fun getDescription(code: Int): String {
        return codeMap[code] ?: "未知天气"
    }
}
