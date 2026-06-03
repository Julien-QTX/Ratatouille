package com.ratacorp.ratatouille

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ratacorp.ratatouille.ui.ViewModelFactory
import com.ratacorp.ratatouille.ui.scan.ScanScreen
import com.ratacorp.ratatouille.ui.scan.ScanViewModel
import com.ratacorp.ratatouille.ui.theme.RatatouilleTheme

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object History : Screen("history", "Historique", Icons.Default.History)
    object Scan : Screen("scan", "Scanner", Icons.Default.QrCodeScanner)
    object Favorites : Screen("favorites", "Favoris", Icons.Default.Favorite)
    object Search : Screen("search", "Recherche", Icons.Default.Search)
}

val items = listOf(
    Screen.History,
    Screen.Scan,
    Screen.Favorites,
    Screen.Search
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as RatatouilleApplication).container
        val viewModelFactory = ViewModelFactory(appContainer.productRepository)

        enableEdgeToEdge()
        setContent {
            RatatouilleTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(screen.label) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.History.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.History.route) { HistoryScreen() }
                        composable(Screen.Scan.route) { 
                            val scanViewModel: ScanViewModel = viewModel(factory = viewModelFactory)
                            ScanScreen(scanViewModel) 
                        }
                        composable(Screen.Favorites.route) { FavoritesScreen() }
                        composable(Screen.Search.route) { SearchScreen() }
                    }
                }
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
