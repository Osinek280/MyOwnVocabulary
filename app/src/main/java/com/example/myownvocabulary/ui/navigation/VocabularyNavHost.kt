package com.example.myownvocabulary.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.myownvocabulary.data.transfer.TransferPreview
import com.example.myownvocabulary.data.transfer.toPreview
import com.example.myownvocabulary.domain.quiz.QuizQuestionGenerator
import com.example.myownvocabulary.model.ContextSentence
import com.example.myownvocabulary.model.Entry
import com.example.myownvocabulary.ui.components.states.EmptyVocabularyState
import com.example.myownvocabulary.ui.components.states.EntryNotFoundState
import com.example.myownvocabulary.ui.components.states.LoadingState
import com.example.myownvocabulary.ui.screens.EntriesListScreen
import com.example.myownvocabulary.ui.screens.EntryDetailScreen
import com.example.myownvocabulary.ui.screens.HomeScreen
import com.example.myownvocabulary.ui.screens.QuizScreen
import com.example.myownvocabulary.ui.screens.SettingsScreen
import com.example.myownvocabulary.ui.screens.TransferScreen
import com.example.myownvocabulary.ui.viewmodel.TransferViewModel
import com.example.myownvocabulary.ui.viewmodel.TransferViewModelFactory
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
    val database = remember { AppDatabase.getInstance(appContext) }
    val viewModel: VocabularyViewModel = viewModel(
        factory = remember {
            VocabularyViewModelFactory(
                dao = database.entryDao(),
                contextDao = database.contextSentenceDao(),
                userPreferences = UserPreferences(appContext)
            )
        }
    )
    val transferViewModel: TransferViewModel = viewModel(
        factory = remember {
            TransferViewModelFactory(
                database = database,
                dao = database.entryDao(),
                contextDao = database.contextSentenceDao()
            )
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentLanguages by viewModel.recentLanguages.collectAsStateWithLifecycle()
    val transfer by transferViewModel.transfer.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) transferViewModel.exportStaged(uri, context.contentResolver)
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            transferViewModel.beginImport(uri, context.contentResolver)
            navController.navigate(Routes.transfer(Routes.TRANSFER_IMPORT))
        }
    }

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
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenVocabulary = { navController.navigateToTab(Routes.ENTRIES) }
                )
            }

            composable(Routes.ENTRIES) {
                val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
                val homeOptions by viewModel.homeOptions.collectAsStateWithLifecycle()
                val loadedHomeOptions = homeOptions
                if (loadedHomeOptions == null) {
                    LoadingState(modifier = Modifier.fillMaxSize())
                    return@composable
                }
                EntriesListScreen(
                    options = loadedHomeOptions,
                    onOptionsChange = viewModel::saveHomeOptions,
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
                val quizOptions by viewModel.quizOptions.collectAsStateWithLifecycle()
                val loadedQuizOptions = quizOptions
                if (loadedQuizOptions == null || uiState.isLoading) {
                    LoadingState(modifier = Modifier.fillMaxSize())
                    return@composable
                }
                val quizQuestions = remember(
                    uiState.entries,
                    loadedQuizOptions.multipleChoice,
                    loadedQuizOptions.written
                ) {
                    QuizQuestionGenerator.generate(uiState.entries, loadedQuizOptions)
                }
                var questionIndex by rememberSaveable(
                    uiState.entries,
                    loadedQuizOptions.multipleChoice,
                    loadedQuizOptions.written
                ) { mutableIntStateOf(0) }
                if (quizQuestions.isEmpty()) {
                    EmptyVocabularyState(
                        onAddClick = { navController.navigate(Routes.addEntry(EntryKind.Word)) }
                    )
                    return@composable
                }
                QuizScreen(
                    quizOptions = loadedQuizOptions,
                    onQuizOptionsChange = viewModel::saveQuizOptions,
                    currentIndex = questionIndex,
                    totalCount = quizQuestions.size,
                    question = quizQuestions[questionIndex.coerceIn(quizQuestions.indices)],
                    onNextQuestion = {
                        if (questionIndex < quizQuestions.lastIndex) {
                            questionIndex++
                        } else {
                            questionIndex = 0
                            navController.navigateToTab(Routes.ENTRIES)
                        }
                    },
                    onRestart = { questionIndex = 0 },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    canExport = uiState.entries.isNotEmpty() && !uiState.isLoading,
                    error = transfer.error,
                    result = transfer.result,
                    onExport = { navController.navigate(Routes.transfer(Routes.TRANSFER_EXPORT)) },
                    onImport = {
                        importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    },
                    onDismissFeedback = transferViewModel::acknowledgeTransferFeedback
                )
            }

            composable(
                Routes.TRANSFER,
                arguments = listOf(navArgument("mode") { type = NavType.StringType })
            ) { entry ->
                val importing = entry.arguments?.getString("mode") == Routes.TRANSFER_IMPORT
                val rows = if (importing) {
                    transfer.importPreview.orEmpty().mapNotNull { it.toPreview() }
                } else {
                    uiState.entries.map { it.toTransferPreview() }
                }
                val existingIds = remember(uiState.entries) { uiState.entries.map { it.id }.toSet() }
                val loadFailed = importing &&
                    transfer.importPreview == null &&
                    !transfer.busy &&
                    transfer.error != null

                BackHandler(enabled = importing) {
                    transferViewModel.discardImport()
                    navController.popBackStack()
                }
                LaunchedEffect(transfer.result) {
                    if (transfer.result != null &&
                        navController.currentDestination?.route == Routes.TRANSFER
                    ) {
                        navController.popBackStack()
                    }
                }
                LaunchedEffect(loadFailed) {
                    if (loadFailed && navController.currentDestination?.route == Routes.TRANSFER) {
                        navController.popBackStack()
                    }
                }

                TransferScreen(
                    title = if (importing) "Import" else "Eksport",
                    confirmLabel = if (importing) "Importuj" else "Zapisz plik",
                    rows = rows,
                    existingIds = existingIds,
                    showImportStatus = importing,
                    isLoading = importing && transfer.importPreview == null && transfer.error == null,
                    busy = transfer.busy,
                    error = if (importing && transfer.importPreview == null) null else transfer.error,
                    onBack = {
                        if (importing) transferViewModel.discardImport()
                        navController.popBackStack()
                    },
                    onConfirm = { ids ->
                        if (importing) {
                            transferViewModel.importSelected(ids)
                        } else {
                            transferViewModel.stageExport(ids)
                            exportLauncher.launch("vocabulary.json")
                        }
                    },
                    onDismissError = transferViewModel::dismissTransferError
                )
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
                        partOfSpeech = PartOfSpeech.Noun.takeIf { kind == EntryKind.Word },
                        kind = kind
                    ),
                    entries = uiState.entries,
                    initialContexts = emptyList(),
                    recentLanguages = recentLanguages,
                    onBack = { navController.popBackStack() },
                    onSave = { term, translation, pos, languageCode, contexts, kind, meaning, numericValue ->
                        viewModel.save(
                            id = null,
                            term = term,
                            translation = translation,
                            pos = pos,
                            languageCode = languageCode,
                            contexts = contexts,
                            kind = kind,
                            meaning = meaning,
                            numericValue = numericValue
                        )
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
                            onSave = { term, translation, pos, languageCode, contexts, kind, meaning, numericValue ->
                                viewModel.save(
                                    id = entry.id,
                                    term = term,
                                    translation = translation,
                                    pos = pos,
                                    languageCode = languageCode,
                                    contexts = contexts,
                                    kind = kind,
                                    meaning = meaning,
                                    numericValue = numericValue
                                )
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

private fun Entry.toTransferPreview() = TransferPreview(
    id = id,
    term = term,
    translation = translation,
    languageCode = languageCode,
    kind = kind,
    partOfSpeech = partOfSpeech
)

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
