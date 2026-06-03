package com.ratacorp.ratatouille.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ScanState {
    object Idle : ScanState()
    object Loading : ScanState()
    data class Success(
        val product: Product,
        val betterAlternative: Product? = null
    ) : ScanState()
    data class Error(val message: String) : ScanState()
}

class ScanViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanState>(ScanState.Idle)
    val uiState: StateFlow<ScanState> = _uiState

    fun scanProduct(barcode: String) {
        viewModelScope.launch {
            _uiState.value = ScanState.Loading
            repository.getProduct(barcode)
                .onSuccess { product ->
                    _uiState.value = ScanState.Success(product)
                    
                    // Si Nutri-Score D ou E, chercher une alternative
                    val grade = product.nutritionGrades?.lowercase()
                    if (grade == "d" || grade == "e") {
                        val alternative = repository.getBetterAlternative(product)
                        if (alternative != null) {
                            _uiState.value = ScanState.Success(product, alternative)
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.value = ScanState.Error(error.message ?: "Une erreur est survenue")
                }
        }
    }

    fun resetState() {
        _uiState.value = ScanState.Idle
    }
}
