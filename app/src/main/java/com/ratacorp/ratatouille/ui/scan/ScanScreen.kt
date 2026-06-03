package com.ratacorp.ratatouille.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.ratacorp.ratatouille.data.model.Product

@Composable
fun ScanScreen(viewModel: ScanViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var showManualEntry by remember { mutableStateOf(false) }
    var manualBarcode by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission && !showManualEntry) {
            CameraPreview(onBarcodeDetected = { barcode ->
                // Éviter les scans multiples trop rapides
                if (uiState !is ScanState.Loading && uiState !is ScanState.Success) {
                    viewModel.scanProduct(barcode)
                }
            })
        } else if (showManualEntry) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Saisie manuelle",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { manualBarcode = it },
                    label = { Text("Code-barres") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        if (manualBarcode.isNotBlank()) {
                            viewModel.scanProduct(manualBarcode)
                            showManualEntry = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rechercher le produit")
                }
                TextButton(onClick = { showManualEntry = false }) {
                    Text("Retour au scanner")
                }
            }
        } else {
            // ... (permission column stays the same)
        }

        // Bouton pour basculer en manuel (en haut à droite)
        if (!showManualEntry) {
            Button(
                onClick = { showManualEntry = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 32.dp, end = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saisir un code", fontSize = 14.sp)
            }
        }

        // Overlay pour afficher le résultat ou le chargement
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is ScanState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ScanState.Success -> {
                    ProductCard(
                        product = state.product,
                        onClose = { viewModel.resetState() }
                    )
                }
                is ScanState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = state.message, color = Color.White, modifier = Modifier.weight(1f))
                            Button(onClick = { viewModel.resetState() }) {
                                Text("Réessayer")
                            }
                        }
                    }
                }
                is ScanState.Idle -> {
                    // Optionnel : petit guide de scan
                    Text(
                        "Placez un code-barres dans le cadre",
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClose: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.productName ?: "Nom inconnu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Text("X") // Remplacer par une icône si possible
                }
            }

            product.brands?.let {
                Text(
                    text = it,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
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

                Column {
                    Text("Nutri-Score", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    NutriScoreBadge(product.nutritionGrades)
                }
            }
        }
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

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}
