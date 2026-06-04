package com.ratacorp.ratatouille.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.ui.components.NutriScoreBadge
import com.ratacorp.ratatouille.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    val query by viewModel.query.collectAsState()
    val products = viewModel.searchResults.collectAsLazyPagingItems()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val alternative by viewModel.alternative.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Barre de recherche
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Ex: chocolate-spreads, beverages...") },
            label = { Text("Rechercher par catégorie") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (query.isBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Saisissez une catégorie pour commencer", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products.itemCount) { index ->
                        products[index]?.let { product ->
                            SearchResultItem(product = product, onClick = { viewModel.selectProduct(product) })
                        }
                    }

                    // État de chargement au bas de la liste
                    products.apply {
                        when {
                            loadState.refresh is LoadState.Loading -> {
                                item { 
                                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator() 
                                    }
                                }
                            }
                            loadState.append is LoadState.Loading -> {
                                item { 
                                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp)) 
                                    }
                                }
                            }
                            loadState.refresh is LoadState.Error -> {
                                val e = loadState.refresh as LoadState.Error
                                item {
                                    Text("Erreur: ${e.error.localizedMessage}", color = Color.Red, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Overlay fiche produit
            selectedProduct?.let { product ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { viewModel.selectProduct(null) }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProductCard(
                        product = product,
                        betterAlternative = alternative,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onClose = { viewModel.selectProduct(null) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(product.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.productName ?: "Inconnu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = product.brands ?: "Marque inconnue",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
            NutriScoreBadge(product.nutritionGrades)
        }
    }
}
