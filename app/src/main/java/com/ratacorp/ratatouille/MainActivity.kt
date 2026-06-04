// MainActivity.kt
package com.ratacorp.ratatouille

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ratacorp.ratatouille.di.AppContainer
import com.ratacorp.ratatouille.ui.history.HistoryScreen
import com.ratacorp.ratatouille.ui.history.HistoryViewModel
import com.ratacorp.ratatouille.ui.scan.ScanScreen
import com.ratacorp.ratatouille.ui.scan.ScanViewModel
import com.ratacorp.ratatouille.ui.theme.RatatouilleTheme
class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = (application as RatatouilleApplication).appContainer

        setContent {
            RatatouilleTheme {
                RatatouilleApp(appContainer)
            }
        }
    }
}

@Composable
fun RatatouilleApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModels instanciés une seule fois via remember
    val scanViewModel = remember {
        ScanViewModel(appContainer.productRepository)
    }
    val historyViewModel = remember {
        HistoryViewModel(appContainer.productRepository)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner") },
                    label = { Text("Scanner") },
                    selected = currentRoute == "scan",
                    onClick = {
                        navController.navigate("scan") {
                            popUpTo("scan") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Historique") },
                    label = { Text("Historique") },
                    selected = currentRoute == "history",
                    onClick = {
                        navController.navigate("history") {
                            popUpTo("scan")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "scan",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("scan") {
                ScanScreen(viewModel = scanViewModel)
            }
            composable("history") {
                HistoryScreen(viewModel = historyViewModel)
            }
        }
    }
}

@Composable
fun HistoryScreen() {
    Text(text = "Écran Historique (Home)")
}

@Composable
fun FavoritesScreen() {
    Text(text = "Écran Favoris")
}

@Composable
fun SearchScreen() {
    Text(text = "Écran Recherche")
}
