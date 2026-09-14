package com.example.myownvocabulary.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myownvocabulary.data.AppDatabase
import com.example.myownvocabulary.ui.screens.AddWordScreen
import com.example.myownvocabulary.ui.screens.HomeScreen
import com.example.myownvocabulary.ui.viewmodel.VocabularyViewModel
import com.example.myownvocabulary.ui.viewmodel.VocabularyViewModelFactory

@Composable
fun VocabularyNavHost() {
    val navController = rememberNavController()
    val onTabSelected: (String) -> Unit = { route ->
        navController.navigateToTab(route)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val colors = MaterialTheme.colorScheme

    val context = LocalContext.current
    val viewModel: VocabularyViewModel = viewModel(
        factory = remember {
            VocabularyViewModelFactory(
                AppDatabase.getInstance(context.applicationContext).wordDao(),
            )
        },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = colors.background,
        contentColor = colors.onBackground,
        bottomBar = {
            if (currentRoute in Routes.Tabs) {
                BottomNav(
                    currentRoute = currentRoute ?: Routes.Words,
                    onNavigate =  onTabSelected,
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Words,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.Words) {
                HomeScreen(
                    words = uiState.words,
                    isLoading = uiState.isLoading,
                    onAddClick = { navController.navigate(Routes.AddWord) },
                )
            }

            composable(Routes.Learn) {
                Text("Learn Page")
            }

            composable(Routes.Settings) {
                Text("Settings Page")
            }

            composable(Routes.AddWord) {
                AddWordScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { term, translation, pos ->
                        viewModel.save(term, translation, pos, "en")
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
