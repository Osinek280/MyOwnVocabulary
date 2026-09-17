package com.example.myownvocabulary.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myownvocabulary.data.AppDatabase
import com.example.myownvocabulary.data.prefs.UserPreferences
import com.example.myownvocabulary.ui.components.states.LoadingState
import com.example.myownvocabulary.ui.components.states.WordNotFoundState
import com.example.myownvocabulary.ui.screens.HomeScreen
import com.example.myownvocabulary.ui.screens.WordDetailScreen
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
    val appContext = context.applicationContext
    val viewModel: VocabularyViewModel = viewModel(
        factory = remember {
            VocabularyViewModelFactory(
                dao = AppDatabase.getInstance(context.applicationContext).wordDao(),
                userPreferences = UserPreferences(appContext)
            )
        },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentLanguages by viewModel.recentLanguages.collectAsStateWithLifecycle()

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
                val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
                HomeScreen(
                    words = uiState.words,
                    isLoading = uiState.isLoading,
                    onAddClick = { navController.navigate(Routes.AddWord) },
                    onWordClick = { wordId -> navController.navigate(Routes.wordDetail(wordId)) },
                    onToggleSelect = viewModel::toggleSelection,
                    onEnterSelection = viewModel::enterSelection,
                    onClearSelection = viewModel::clearSelection,
                    onDeleteSelected = viewModel::deleteSelected,
                    selectedIds = selectedIds,
                )
            }

            composable(Routes.Learn) {
                Text("Learn Page")
            }

            composable(Routes.Settings) {
                Text("Settings Page")
            }

            composable(Routes.AddWord) {
                WordDetailScreen(
                    words = uiState.words,
                    recentLanguages = recentLanguages,
                    onBack = { navController.popBackStack() },
                    onSave = { term, translation, pos, languageCode ->
                        viewModel.save(id=null, term, translation, pos, languageCode)
                        navController.popBackStack()
                    },
                    onLanguageRemembered = viewModel::rememberLanguage,
                    onClearRecent = viewModel::clearRecentLanguages
                )
            }

            composable(
                Routes.WordDetail,
                arguments = listOf(navArgument("wordId") { type = NavType.StringType }),
            ) { entry ->
                val wordId = entry.arguments?.getString("wordId")
                val word = uiState.words.find { it.id == wordId }

                when {
                    word != null -> {
                        WordDetailScreen(
                            word = word,
                            words = uiState.words,
                            onSave = { term, translation, pos, languageCode ->
                                viewModel.save(id = word.id, term, translation, pos, languageCode)
                                navController.popBackStack()
                            },
                            recentLanguages = recentLanguages,
                            onBack = { navController.popBackStack() },
                            onLanguageRemembered = viewModel::rememberLanguage,
                            onClearRecent = viewModel::clearRecentLanguages,
                        )
                    }
                    uiState.isLoading -> {
                        LoadingState(modifier = Modifier.fillMaxSize())
                    }
                    else -> {
                        WordNotFoundState (onBack = { navController.popBackStack() })
                    }
                }
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
