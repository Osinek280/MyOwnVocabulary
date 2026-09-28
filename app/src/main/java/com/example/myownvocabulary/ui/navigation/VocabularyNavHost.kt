package com.example.myownvocabulary.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.PartOfSpeech
import com.example.myownvocabulary.data.prefs.UserPreferences
import com.example.myownvocabulary.model.ContextSentence
import com.example.myownvocabulary.model.Entry
import com.example.myownvocabulary.ui.components.states.EntryNotFoundState
import com.example.myownvocabulary.ui.components.states.LoadingState
import com.example.myownvocabulary.ui.screens.EntryDetailScreen
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
    val appContext = context.applicationContext
    val viewModel: VocabularyViewModel = viewModel(
        factory = remember {
            VocabularyViewModelFactory(
                dao = AppDatabase.getInstance(context.applicationContext).entryDao(),
                contextDao = AppDatabase.getInstance(context.applicationContext).contextSentenceDao(),
                userPreferences = UserPreferences(appContext)
            )
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentLanguages by viewModel.recentLanguages.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = colors.background,
        contentColor = colors.onBackground,
        bottomBar = {
            if (currentRoute in Routes.Tabs) {
                BottomNav(
                    currentRoute = currentRoute ?: Routes.ENTRIES,
                    onNavigate = onTabSelected
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ENTRIES,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.ENTRIES) {
                val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
                HomeScreen(
                    entries = uiState.entries,
                    isLoading = uiState.isLoading,
                    onAddClick = { kind -> navController.navigate(Routes.addEntry(kind)) },
                    onEntryClick = { entryId -> navController.navigate(Routes.entryDetail(entryId)) },
                    onToggleSelect = viewModel::toggleSelection,
                    onEnterSelection = viewModel::enterSelection,
                    onClearSelection = viewModel::clearSelection,
                    onDeleteSelected = viewModel::deleteSelected,
                    selectedIds = selectedIds
                )
            }

            composable(Routes.LEARN) {
                Text("Learn Page")
            }

            composable(Routes.SETTINGS) {
                Text("Settings Page")
            }

            composable(
                Routes.ADD_ENTRY,
                arguments = listOf(navArgument("kind") { type = NavType.StringType })
            ) { backStackEntry ->
                val kind = EntryKind.valueOf(
                    backStackEntry.arguments?.getString("kind") ?: EntryKind.Word.name
                )

                EntryDetailScreen(
                    entry = Entry(
                        id = "",
                        term = "",
                        translation = "",
                        languageCode = "en",
                        partOfSpeech = PartOfSpeech.Noun,
                        kind = kind
                    ),
                    entries = uiState.entries,
                    initialContexts = emptyList(),
                    recentLanguages = recentLanguages,
                    onBack = { navController.popBackStack() },
                    onSave = { term, translation, pos, languageCode, contexts, kind ->
                        viewModel.save(id = null, term, translation, pos, languageCode, contexts, kind)
                        navController.popBackStack()
                    },
                    onLanguageRemembered = viewModel::rememberLanguage,
                    onClearRecent = viewModel::clearRecentLanguages
                )
            }

            composable(
                Routes.ENTRY_DETAIL,
                arguments = listOf(navArgument("entryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId")
                val entry = uiState.entries.find { it.id == entryId }

                val contexts by produceState<List<ContextSentence>?>(null, entryId) {
                    value = if (entryId == null) emptyList() else viewModel.loadContexts(entryId)
                }

                val loadedContexts = contexts

                when {
                    entry != null && loadedContexts != null -> {
                        EntryDetailScreen(
                            entry = entry,
                            initialContexts = loadedContexts,
                            entries = uiState.entries,
                            onSave = { term, translation, pos, languageCode, contexts, kind ->
                                viewModel.save(id = entry.id, term, translation, pos, languageCode, contexts, kind)
                                navController.popBackStack()
                            },
                            recentLanguages = recentLanguages,
                            onBack = { navController.popBackStack() },
                            onLanguageRemembered = viewModel::rememberLanguage,
                            onClearRecent = viewModel::clearRecentLanguages
                        )
                    }
                    uiState.isLoading -> {
                        LoadingState(modifier = Modifier.fillMaxSize())
                    }
                    else -> {
                        EntryNotFoundState(onBack = { navController.popBackStack() })
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
