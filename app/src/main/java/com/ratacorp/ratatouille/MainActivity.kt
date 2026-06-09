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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.ratacorp.ratatouille.ui.favorites.FavoritesScreen
import com.ratacorp.ratatouille.ui.favorites.FavoritesViewModel
import com.ratacorp.ratatouille.ui.history.HistoryScreen
import com.ratacorp.ratatouille.ui.history.HistoryViewModel
import com.ratacorp.ratatouille.ui.scan.ScanScreen
import com.ratacorp.ratatouille.ui.scan.ScanViewModel
import com.ratacorp.ratatouille.ui.search.SearchScreen
import com.ratacorp.ratatouille.ui.search.SearchViewModel
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
                        composable(Screen.History.route) { 
                            val historyViewModel: HistoryViewModel = viewModel(factory = viewModelFactory)
                            HistoryScreen(viewModel = historyViewModel) 
                        }
                        composable(
                            route = Screen.Scan.route + "?barcode={barcode}",
                            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "myapp://product/{barcode}" })
                        ) { backStackEntry -> 
                            val scanViewModel: ScanViewModel = viewModel(factory = viewModelFactory)
                            val barcode = backStackEntry.arguments?.getString("barcode")
                            
                            LaunchedEffect(barcode) {
                                if (barcode != null) {
                                    scanViewModel.scanProduct(barcode)
                                }
                            }
                            
                            ScanScreen(scanViewModel) 
                        }
                        composable(Screen.Favorites.route) { 
                            val favoritesViewModel: FavoritesViewModel = viewModel(factory = viewModelFactory)
                            FavoritesScreen(favoritesViewModel) 
                        }
                        composable(Screen.Search.route) { 
                            val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
                            SearchScreen(searchViewModel) 
                        }
                    }
                }
            }
        }
    }
}
