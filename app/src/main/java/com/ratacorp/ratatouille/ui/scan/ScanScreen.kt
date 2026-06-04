package com.ratacorp.ratatouille.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ratacorp.ratatouille.ui.components.ProductCard

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
        viewModel.resetState()
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var showManualEntry by remember { mutableStateOf(false) }
    var manualBarcode by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission && !showManualEntry) {
            CameraPreview(onBarcodeDetected = { barcode ->
                // On ne scanne que si on est en état IDLE
                // Cela évite de rescanner en boucle en cas d'erreur (ex: 429) ou de chargement
                if (uiState is ScanState.Idle) {
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
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Permission caméra requise pour scanner")
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Autoriser")
                }
            }
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
                        betterAlternative = state.betterAlternative,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
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
                    Text(
                        "Placez un code-barres devant l'appareil photo",
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
