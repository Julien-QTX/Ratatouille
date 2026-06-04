package com.ratacorp.ratatouille.ui.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.rememberAsyncImagePainter
import com.ratacorp.ratatouille.data.model.Product
import com.ratacorp.ratatouille.ui.components.NutriScoreBadge
import com.ratacorp.ratatouille.ui.components.ProductCard

@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel) {
    val favorites by viewModel.favoritesState.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val alternative by viewModel.alternative.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun favori pour le moment")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Mes Favoris",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                items(favorites) { product ->
                    FavoriteItem(product = product, onClick = { viewModel.selectProduct(product) })
                }
            }
        }

        // Overlay pour afficher la fiche produit quand on clique sur un item
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

@Composable
fun FavoriteItem(product: Product, onClick: () -> Unit) {
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
