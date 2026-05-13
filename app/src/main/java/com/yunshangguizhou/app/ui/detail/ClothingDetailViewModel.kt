package com.yunshangguizhou.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunshangguizhou.app.data.local.entity.ClothingEntity
import com.yunshangguizhou.app.data.repository.ClothingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClothingDetailUiState(
    val clothing: ClothingEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ClothingDetailViewModel @Inject constructor(
    private val clothingRepository: ClothingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClothingDetailUiState())
    val uiState: StateFlow<ClothingDetailUiState> = _uiState.asStateFlow()

    fun loadClothing(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val clothing = clothingRepository.getClothingById(id)
                _uiState.update {
                    it.copy(clothing = clothing, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun markWashing(isWashing: Boolean) {
        val clothing = _uiState.value.clothing ?: return
        viewModelScope.launch {
            clothingRepository.markWashing(clothing.id, isWashing)
            loadClothing(clothing.id)
        }
    }

    fun deleteClothing() {
        val clothing = _uiState.value.clothing ?: return
        viewModelScope.launch {
            clothingRepository.deleteClothing(clothing)
        }
    }
}
