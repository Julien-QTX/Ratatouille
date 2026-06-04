package com.ratacorp.ratatouille.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScanState {
    object Idle : ScanState
    object Loading : ScanState
    data class Success(val product: Product) : ScanState
    data class Error(val message: String) : ScanState
}

class ScanViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanState>(ScanState.Idle)
    val uiState: StateFlow<ScanState> = _uiState.asStateFlow()

    fun scanProduct(barcode: String) {
        viewModelScope.launch {
            _uiState.value = ScanState.Loading
            repository.getProduct(barcode)
                .onSuccess { product ->
                    _uiState.value = ScanState.Success(product)
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
