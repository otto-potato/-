package com.yunshangguizhou.app.ui.wardrobe

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunshangguizhou.app.data.repository.ClothingRepository
import com.yunshangguizhou.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddUiState(
    val name: String = "", val category: String = "上衣", val color: String = "",
    val material: String = "", val thickness: String = "适中", val season: String = "春",
    val style: String = "休闲", val description: String = "", val imageUri: Uri? = null,
    val loading: Boolean = false, val success: Boolean = false,
    val aiSuccess: Boolean = false, val error: String? = null
)

val clothingCategories = listOf("上衣", "裤子", "裙子", "外套", "鞋子", "配饰")
val thicknessOptions = listOf("薄", "适中", "厚")
val seasonOptions = listOf("春", "夏", "秋", "冬")
val styleOptions = listOf("休闲", "正式", "运动", "商务", "居家", "街头")
val colorOptions = listOf("黑色", "白色", "灰色", "红色", "蓝色", "绿色", "黄色", "粉色", "紫色", "橙色", "棕色", "米色", "卡其色", "藏青色", "军绿色")
val materialOptions = listOf("棉", "麻", "丝绸", "羊毛", "羊绒", "涤纶", "尼龙", "牛仔布", "皮革", "绒面", "雪纺", "针织", "灯芯绒")

@HiltViewModel
class AddClothingViewModel @Inject constructor(
    private val repo: ClothingRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _s = MutableStateFlow(AddUiState())
    val state: StateFlow<AddUiState> = _s.asStateFlow()

    fun setName(v: String) { _s.update { it.copy(name = v) } }
    fun setCategory(v: String) { _s.update { it.copy(category = v) } }
    fun setColor(v: String) { _s.update { it.copy(color = v) } }
    fun setMaterial(v: String) { _s.update { it.copy(material = v) } }
    fun setThickness(v: String) { _s.update { it.copy(thickness = v) } }
    fun setSeason(v: String) { _s.update { it.copy(season = v) } }
    fun setStyle(v: String) { _s.update { it.copy(style = v) } }
    fun setDesc(v: String) { _s.update { it.copy(description = v) } }
    fun setImage(u: Uri?) { _s.update { it.copy(imageUri = u) } }

    fun analyzeWithAi() {
        val s = _s.value
        if (s.name.isBlank()) { _s.update { it.copy(error = "请输入名称") }; return }
        viewModelScope.launch {
            _s.update { it.copy(loading = true, error = null) }
            val cfg = settingsRepo.getSettingsOnce()
            if (cfg.aiApiUrl.isBlank() || cfg.aiApiKey.isBlank()) {
                _s.update { it.copy(loading = false, error = "请先配置AI模型") }; return@launch
            }
            repo.addWithAi(s.name, s.category, s.color.ifEmpty { "未知" }, s.material.ifEmpty { "未知" },
                s.thickness, s.season, s.style, s.imageUri?.toString(), cfg.aiApiUrl, cfg.aiApiKey, cfg.aiModelName
            ).fold(
                onSuccess = { (_, aiOk) ->
                    _s.update { it.copy(loading = false, success = true, aiSuccess = aiOk,
                        error = if (!aiOk) "AI分析失败，已保存基本信息" else null) }
                },
                onFailure = { e -> _s.update { it.copy(loading = false, error = e.message) } }
            )
        }
    }

    fun saveManually() {
        val s = _s.value
        if (s.name.isBlank()) { _s.update { it.copy(error = "请输入名称") }; return }
        viewModelScope.launch {
            _s.update { it.copy(loading = true, error = null) }
            repo.addManual(s.name, s.category, s.color.ifEmpty { "未知" }, s.material.ifEmpty { "未知" },
                s.thickness, s.season, s.style,
                s.description.ifEmpty { s.name }, s.imageUri?.toString())
            _s.update { it.copy(loading = false, success = true) }
        }
    }

    fun reset() { _s.value = AddUiState() }
}
