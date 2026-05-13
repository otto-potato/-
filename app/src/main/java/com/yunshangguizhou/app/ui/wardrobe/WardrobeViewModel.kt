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
    val allClothing: List<ClothingEntity> = emptyList(),
    val filteredClothing: List<ClothingEntity> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val clothingRepository: ClothingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WardrobeUiState())
    val uiState: StateFlow<WardrobeUiState> = _uiState.asStateFlow()

    init {
        loadClothing()
    }

    private fun loadClothing() {
        viewModelScope.launch {
            clothingRepository.getAllClothing().collect { clothes ->
                val filtered = if (_uiState.value.selectedCategory != null) {
                    clothes.filter { it.category == _uiState.value.selectedCategory }
                } else {
                    clothes
                }
                _uiState.update {
                    it.copy(allClothing = clothes, filteredClothing = filtered, isLoading = false)
                }
            }
        }
    }

    fun filterByCategory(category: String?) {
        _uiState.update { state ->
            val filtered = if (category != null) {
                state.allClothing.filter { it.category == category }
            } else {
                state.allClothing
            }
            state.copy(selectedCategory = category, filteredClothing = filtered)
        }
    }

    fun deleteClothing(clothing: ClothingEntity) {
        viewModelScope.launch {
            clothingRepository.deleteClothing(clothing)
        }
    }

    fun markWashing(clothingId: Long, isWashing: Boolean) {
        viewModelScope.launch {
            clothingRepository.markWashing(clothingId, isWashing)
        }
    }
}
