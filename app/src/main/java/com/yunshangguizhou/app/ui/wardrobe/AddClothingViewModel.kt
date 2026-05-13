package com.yunshangguizhou.app.ui.wardrobe

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunshangguizhou.app.data.remote.ClothingAnalysis
import com.yunshangguizhou.app.data.repository.ClothingRepository
import com.yunshangguizhou.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddClothingUiState(
    val name: String = "",
    val category: String = "上衣",
    val color: String = "",
    val material: String = "",
    val thickness: String = "适中",
    val season: String = "春",
    val style: String = "休闲",
    val description: String = "",
    val imageUri: Uri? = null,
    val isAnalyzing: Boolean = false,
    val analysisResult: ClothingAnalysis? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

val clothingCategories = listOf("上衣", "裤子", "裙子", "外套", "鞋子", "配饰")
val thicknessOptions = listOf("薄", "适中", "厚")
val seasonOptions = listOf("春", "夏", "秋", "冬")
val styleOptions = listOf("休闲", "正式", "运动", "商务", "居家", "街头")
val colorOptions = listOf(
    "黑色", "白色", "灰色", "红色", "蓝色", "绿色", "黄色", "粉色",
    "紫色", "橙色", "棕色", "米色", "卡其色", "藏青色", "军绿色"
)
val materialOptions = listOf(
    "棉", "麻", "丝绸", "羊毛", "羊绒", "涤纶", "尼龙",
    "牛仔布", "皮革", "绒面", "雪纺", "针织", "灯芯绒"
)

@HiltViewModel
class AddClothingViewModel @Inject constructor(
    private val clothingRepository: ClothingRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddClothingUiState())
    val uiState: StateFlow<AddClothingUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateColor(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun updateMaterial(material: String) {
        _uiState.update { it.copy(material = material) }
    }

    fun updateThickness(thickness: String) {
        _uiState.update { it.copy(thickness = thickness) }
    }

    fun updateSeason(season: String) {
        _uiState.update { it.copy(season = season) }
    }

    fun updateStyle(style: String) {
        _uiState.update { it.copy(style = style) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun setImageUri(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun analyzeWithAi() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "请输入衣物名称") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null) }

            try {
                val settings = settingsRepository.getSettingsOnce()
                if (settings.aiApiUrl.isBlank() || settings.aiApiKey.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            error = "请先在设置中配置AI模型信息"
                        )
                    }
                    return@launch
                }

                val result = clothingRepository.addClothingWithAi(
                    name = state.name,
                    category = state.category,
                    color = state.color.ifEmpty { "未知" },
                    material = state.material.ifEmpty { "未知" },
                    thickness = state.thickness,
                    season = state.season,
                    style = state.style,
                    imageUri = state.imageUri?.toString(),
                    aiUrl = settings.aiApiUrl,
                    aiKey = settings.aiApiKey,
                    modelName = settings.aiModelName
                )

                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(isAnalyzing = false, saveSuccess = true)
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isAnalyzing = false, error = e.message)
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isAnalyzing = false, error = e.message)
                }
            }
        }
    }

    fun saveManually() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "请输入衣物名称") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                clothingRepository.addClothingManual(
                    name = state.name,
                    category = state.category,
                    color = state.color.ifEmpty { "未知" },
                    material = state.material.ifEmpty { "未知" },
                    thickness = state.thickness,
                    season = state.season,
                    style = state.style,
                    description = state.description.ifEmpty {
                        "${state.name}，${state.color}色${state.category}，${state.material}材质，适合${state.season}穿着"
                    },
                    imageUri = state.imageUri?.toString()
                )
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun resetForm() {
        _uiState.value = AddClothingUiState()
    }
}
