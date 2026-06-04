package com.ratacorp.ratatouille.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: ProductRepository) : ViewModel() {

    val favoritesState: StateFlow<List<Product>> = repository.getAllFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    private val _alternative = MutableStateFlow<Product?>(null)
    val alternative = _alternative.asStateFlow()

    private var recommendationJob: Job? = null

    fun selectProduct(product: Product?) {
        recommendationJob?.cancel()
        _selectedProduct.value = product
        _alternative.value = null
        
        if (product != null) {
            recommendationJob = viewModelScope.launch {
                _alternative.value = repository.getBetterAlternative(product)
            }
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            repository.toggleFavorite(product)
            // Si on retire des favoris depuis cet écran, on ferme la fiche
            if (_selectedProduct.value?.code == product.code) {
                _selectedProduct.value = null
            }
        }
    }
}
