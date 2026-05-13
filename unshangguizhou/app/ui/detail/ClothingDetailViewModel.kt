package com.yunshangguizhou.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.data.repository.ClothingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val clothing: ClothingEntity? = null,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val editMode: Boolean = false,
    val editName: String = "",
    val editCategory: String = "",
    val editColor: String = "",
    val editMaterial: String = "",
    val editSeason: String = "",
    val editStyle: String = "",
    val editThickness: String = "",
    val editDesc: String = "",
    val error: String? = null
)

@HiltViewModel
class ClothingDetailViewModel @Inject constructor(
    private val repo: ClothingRepository
) : ViewModel() {

    private val _s = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _s.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            _s.update { it.copy(loading = true) }
            val c = repo.getById(id)
            _s.update { it.copy(clothing = c, loading = false,
                editName = c?.name ?: "", editCategory = c?.category ?: "",
                editColor = c?.color ?: "", editMaterial = c?.material ?: "",
                editSeason = c?.season ?: "", editStyle = c?.style ?: "",
                editThickness = c?.thickness ?: "", editDesc = c?.description ?: "") }
        }
    }

    fun toggleWash(washing: Boolean) {
        val c = _s.value.clothing ?: return
        viewModelScope.launch { repo.toggleWash(c.id, washing); load(c.id) }
    }

    fun delete() {
        _s.value.clothing?.let { viewModelScope.launch { repo.delete(it) } }
    }

    fun startEdit() { _s.update { it.copy(editMode = true) } }
    fun cancelEdit() { _s.update { it.copy(editMode = false) } }

    fun setEditName(v: String) { _s.update { it.copy(editName = v) } }
    fun setEditCat(v: String) { _s.update { it.copy(editCategory = v) } }
    fun setEditColor(v: String) { _s.update { it.copy(editColor = v) } }
    fun setEditMat(v: String) { _s.update { it.copy(editMaterial = v) } }
    fun setEditSeason(v: String) { _s.update { it.copy(editSeason = v) } }
    fun setEditStyle(v: String) { _s.update { it.copy(editStyle = v) } }
    fun setEditThick(v: String) { _s.update { it.copy(editThickness = v) } }
    fun setEditDesc(v: String) { _s.update { it.copy(editDesc = v) } }

    fun saveEdit() {
        val s = _s.value; val c = s.clothing ?: return
        viewModelScope.launch {
            _s.update { it.copy(saving = true) }
            repo.update(c.copy(name = s.editName, category = s.editCategory,
                color = s.editColor, material = s.editMaterial, season = s.editSeason,
                style = s.editStyle, thickness = s.editThickness, description = s.editDesc))
            load(c.id)
            _s.update { it.copy(editMode = false, saving = false) }
        }
    }
}
