package com.yunshangguizhou.app.data.remote

import com.yunshangguizhou.app.ui.debug.DebugLog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = GsonBuilder().setLenient().create()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private fun url(base: String): String {
        val t = base.trimEnd('/')
        return when {
            t.endsWith("/v1/chat/completions") -> t
            t.endsWith("/v1") -> "$t/chat/completions"
            else -> "$t/v1/chat/completions"
        }
    }

    private fun call(apiUrl: String, apiKey: String, model: String, sys: String, usr: String): String {
        val body = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to sys),
                mapOf("role" to "user", "content" to usr)
            ),
            "temperature" to 0.7,
            "max_tokens" to 2000
        )
        val json = gson.toJson(body)
        val fullUrl = url(apiUrl)
        val req = Request.Builder().url(fullUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(json.toRequestBody(jsonType)).build()
        val resp = client.newCall(req).execute()
        val raw = resp.body?.string() ?: ""
        DebugLog.log("AI", "url=$fullUrl code=${resp.code} body_size=${json.length}")
        if (!resp.isSuccessful) {
            DebugLog.log("AI-ERR", "code=${resp.code} resp=$raw")
            throw Exception("HTTP ${resp.code} url=$fullUrl body=$json resp=$raw")
        }
        val cr = gson.fromJson(raw, ChatResponse::class.java)
        return cr?.choices?.firstOrNull()?.message?.content ?: throw Exception("No content in: $raw")
    }

    suspend fun analyzeClothing(
        apiUrl: String, apiKey: String, modelName: String,
        name: String, category: String, color: String, material: String,
        thickness: String, season: String, style: String
    ): Result<ClothingAnalysis> = withContext(Dispatchers.IO) {
        try {
            val sys = "You are a clothing analyst. Return ONLY a JSON object with: name, category, color, material, thickness, season, style, description(50-100 Chinese chars). No other text."
            val usr = "Name:$name\nCategory:$category\nColor:$color\nMaterial:$material\nThickness:$thickness\nSeason:$season\nStyle:$style\n\nReturn JSON only."
            val raw = call(apiUrl, apiKey, modelName, sys, usr)
            Result.success(gson.fromJson(json(raw), ClothingAnalysis::class.java))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun generateOutfitRecommendation(
        apiUrl: String, apiKey: String, modelName: String,
        weatherInfo: WeatherInfo, clothes: List<Map<String, Any>>
    ): Result<OutfitRecommendation> = withContext(Dispatchers.IO) {
        try {
            val wx = "${weatherInfo.weatherDesc} ${weatherInfo.minTemp.toInt()}~${weatherInfo.maxTemp.toInt()}C humidity:${weatherInfo.humidity}%"
            val cl = clothes.mapIndexed { _, c ->
                "${c["name"]}(${c["category"]},${c["color"]},${c["thickness"]},worn${c["consecutiveWearDays"]}d)"
            }.joinToString("\n")
            val sys = "You are an outfit advisor. Return ONLY JSON: {\"reasoning\":\"brief reasoning in Chinese\",\"topName\":\"exact item name\",\"bottomName\":\"exact item name\",\"outerwearName\":null,\"shoesName\":\"exact item name\",\"accessoryName\":null}. Copy names exactly as given. No other output."
            val usr = "Weather: $wx\n\nClothes: $cl\n\nRecommend outfit."
            val raw = call(apiUrl, apiKey, modelName, sys, usr)
            Result.success(gson.fromJson(json(raw), OutfitRecommendation::class.java))
        } catch (e: Exception) { Result.failure(e) }
    }

    fun json(raw: String): String {
        var t = raw.trim()
        listOf("```json", "```").forEach { m ->
            val s = t.indexOf(m)
            if (s >= 0) { val e = t.indexOf("```", s + m.length); if (e >= 0) t = t.substring(s + m.length, e).trim() }
        }
        val a = t.indexOf('{'); val b = t.lastIndexOf('}')
        return if (a >= 0 && b > a) t.substring(a, b + 1) else t
    }
}
