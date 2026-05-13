package com.yunshangguizhou.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface QWeatherApiService {

    @GET("v2/city/lookup")
    suspend fun cityLookup(
        @Query("location") location: String,
        @Query("key") apiKey: String
    ): QWeatherGeoResponse

    @GET("v7/weather/3d")
    suspend fun weather3d(
        @Query("location") locationId: String,
        @Query("key") apiKey: String
    ): QWeatherDailyResponse

    @GET("v7/weather/7d")
    suspend fun weather7d(
        @Query("location") locationId: String,
        @Query("key") apiKey: String
    ): QWeatherDailyResponse
}
