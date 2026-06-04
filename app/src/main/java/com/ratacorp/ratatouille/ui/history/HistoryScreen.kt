package com.ratacorp.ratatouille.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val history by viewModel.historyState.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val alternative by viewModel.alternative.collectAsState()
    
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Dialog de confirmation
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Supprimer de l'historique") },
            text = { Text("Voulez-vous vraiment supprimer ce produit de l'historique ?") },
            confirmButton = {
                TextButton(onClick = {
                    productToDelete?.let { viewModel.deleteProduct(it) }
                    productToDelete = null
                }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Structure simplifiée pour éviter les erreurs de mesure (NaN) sur Android 15/16
    Box(modifier = Modifier.fillMaxSize()) {
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun produit scanné pour le moment")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Historique des scans",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                items(history, key = { it.code }) { product ->
                    SwipeableHistoryItem(
                        product = product,
                        onClick = { viewModel.selectProduct(product) },
                        onToggleFavorite = { viewModel.toggleFavorite(product) },
                        onDeleteRequest = { productToDelete = product }
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableHistoryItem(
    product: Product, 
    onClick: () -> Unit, 
    onToggleFavorite: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
            }
            false // Retourne toujours false pour que l'item revienne à sa place en attendant la confirmation
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        HistoryItem(product = product, onClick = onClick, onToggleFavorite = onToggleFavorite)
    }
}

@Composable
fun HistoryItem(product: Product, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
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
            
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (product.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favori",
                    tint = if (product.isFavorite) Color(0xFFFFD700) else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            NutriScoreBadge(product.nutritionGrades)
        }
    }
}
