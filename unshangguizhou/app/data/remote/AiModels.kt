package com.yunshangguizhou.app.data.remote

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 2000
)

data class ChatResponse(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<Choice>? = null
) {
    data class Choice(
        val index: Int? = null,
        val message: ChatMessage? = null,
        val finish_reason: String? = null
    )
}

data class ClothingAnalysis(
    val name: String = "",
    val category: String = "",
    val color: String = "",
    val material: String = "",
    val thickness: String = "",
    val season: String = "",
    val style: String = "",
    val description: String = ""
)

data class OutfitRecommendation(
    val reasoning: String = "",
    val topName: String? = null,
    val bottomName: String? = null,
    val outerwearName: String? = null,
    val shoesName: String? = null,
    val accessoryName: String? = null
)
