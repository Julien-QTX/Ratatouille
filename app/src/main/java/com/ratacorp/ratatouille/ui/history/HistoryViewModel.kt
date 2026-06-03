package com.ratacorp.ratatouille.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: ProductRepository) : ViewModel() {

    val historyState: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}
