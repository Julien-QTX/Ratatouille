package com.ratacorp.ratatouille.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.CloudOff

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Share

@Composable
fun ProductCard(
    product: Product, 
    betterAlternative: Product? = null,
    onToggleFavorite: (Product) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Indicateur Hors ligne
            if (product.isOffline) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Hors ligne",
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Données hors-ligne", fontSize = 10.sp, color = Color.Gray)
                }
            }

            // En-tête : Nom, Favoris, Fermer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.productName ?: "Nom inconnu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = "${product.brands ?: "Marque inconnue"}${if (product.quantity != null) " - ${product.quantity}" else ""}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Row {
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Regarde ce produit génial : myapp://product/${product.code}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Partager")
                    }
                    IconButton(onClick = { onToggleFavorite(product) }) {
                        Icon(
                            imageVector = if (product.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favori",
                            tint = if (product.isFavorite) Color(0xFFFFD700) else Color.Gray
                        )
                    }
                    IconButton(onClick = onClose) {
                        Text("X", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Image et Scores principaux
            Row(modifier = Modifier.fillMaxWidth()) {
                product.imageUrl?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column {
                        Text("Nutri-Score", style = MaterialTheme.typography.labelSmall)
                        NutriScoreBadge(product.nutritionGrades)
                    }
                }
            }

            // Nutriments (version simplifiée)
            product.nutriments?.let { nut ->
                Spacer(modifier = Modifier.height(16.dp))
                Text("Valeurs pour 100g", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    NutrientInfo("Énergie", "${nut.energyKcal?.toInt() ?: "?"} kcal")
                    NutrientInfo("Graisses", "${nut.fat ?: "?"}g")
                    NutrientInfo("Sucres", "${nut.sugars ?: "?"}g")
                    NutrientInfo("Sel", "${nut.salt ?: "?"}g")
                }
            }

            // Bandeau "Meilleure alternative"
            betterAlternative?.let { alt ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        alt.imageUrl?.let {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Meilleure alternative", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text(alt.productName ?: "Inconnu", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                        NutriScoreBadge(alt.nutritionGrades)
                    }
                }
            }
        }
    }
}

@Composable
fun NutrientInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun NutriScoreBadge(grade: String?) {
    val (color, label) = when (grade?.lowercase()) {
        "a" -> Color(0xFF038141) to "A"
        "b" -> Color(0xFF85BB2F) to "B"
        "c" -> Color(0xFFFECB02) to "C"
        "d" -> Color(0xFFEE8100) to "D"
        "e" -> Color(0xFFE63E11) to "E"
        else -> Color.Gray to "?"
    }
    BadgeBox(color, label)
}

@Composable
fun BadgeBox(color: Color, label: String) {
    Box(
        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
