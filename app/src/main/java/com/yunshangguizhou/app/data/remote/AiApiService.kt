package com.yunshangguizhou.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AiApiService {
    @POST("chat/completions")
    suspend fun chatCompletion(@Body request: ChatRequest): ChatResponse
}
