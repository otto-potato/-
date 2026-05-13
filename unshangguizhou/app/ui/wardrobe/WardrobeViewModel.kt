package com.yunshangguizhou.app.ui.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.data.repository.ClothingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WardrobeUiState(
    val all: List<ClothingEntity> = emptyList(),
    val filtered: List<ClothingEntity> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val loading: Boolean = false
)

@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val repo: ClothingRepository
) : ViewModel() {

    private val _s = MutableStateFlow(WardrobeUiState())
    val state: StateFlow<WardrobeUiState> = _s.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAll().collect { list ->
                _s.update { applyFilter(it.copy(all = list)) }
            }
        }
    }

    fun setCategory(c: String?) { _s.update { applyFilter(it.copy(selectedCategory = c)) } }
    fun setSearch(q: String) { _s.update { applyFilter(it.copy(searchQuery = q)) } }

    fun delete(c: ClothingEntity) { viewModelScope.launch { repo.delete(c) } }
    fun toggleWash(id: Long, washing: Boolean) { viewModelScope.launch { repo.toggleWash(id, washing) } }

    private fun applyFilter(s: WardrobeUiState): WardrobeUiState {
        var list = s.all
        if (s.selectedCategory != null) list = list.filter { it.category == s.selectedCategory }
        if (s.searchQuery.isNotBlank()) list = list.filter { it.name.contains(s.searchQuery, true) }
        return s.copy(filtered = list)
    }
}
