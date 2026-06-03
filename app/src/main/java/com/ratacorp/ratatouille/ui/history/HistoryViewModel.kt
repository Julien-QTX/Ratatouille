package com.ratacorp.ratatouille.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: ProductRepository) : ViewModel() {

    val historyState: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    private val _alternative = MutableStateFlow<Product?>(null)
    val alternative = _alternative.asStateFlow()

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
        _alternative.value = null
        if (product != null) {
            viewModelScope.launch {
                _alternative.value = repository.getBetterAlternative(product)
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}
