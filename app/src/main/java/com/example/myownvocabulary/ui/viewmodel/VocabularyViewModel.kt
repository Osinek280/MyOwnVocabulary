package com.example.myownvocabulary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myownvocabulary.data.context.ContextSentenceDao
import com.example.myownvocabulary.data.entry.EntryDao
import com.example.myownvocabulary.data.entry.EntryEntity
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.Language
import com.example.myownvocabulary.data.entry.PartOfSpeech
import com.example.myownvocabulary.data.prefs.UserPreferences
import com.example.myownvocabulary.model.ContextSentence
import com.example.myownvocabulary.model.Entry
import com.example.myownvocabulary.model.toEntity
import com.example.myownvocabulary.model.toModel
import com.example.myownvocabulary.ui.components.quiz.QuizOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntriesUiState(val entries: List<Entry> = emptyList(), val isLoading: Boolean = true)

class VocabularyViewModel(
    private val dao: EntryDao,
    private val contextDao: ContextSentenceDao,
    private val userPreferences: UserPreferences
) : ViewModel() {
    val quizOptions: StateFlow<QuizOptions?> = userPreferences.quizOptions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun saveQuizOptions(options: QuizOptions) {
        viewModelScope.launch {
            userPreferences.saveQuizOptions(options)
        }
    }

    val uiState: StateFlow<EntriesUiState> = dao.observeAll()
        .map { list ->
            EntriesUiState(
                entries = list.map { it.toModel() },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EntriesUiState(isLoading = true)
        )

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    suspend fun loadContexts(entryId: String): List<ContextSentence> =
        contextDao.getByEntryId(entryId).map { it.toModel() }

    fun toggleSelection(id: String) {
        _selectedIds.update { current ->
            if (id in current) current - id else current + id
        }
    }

    fun enterSelection(id: String) {
        _selectedIds.value = setOf(id)
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val ids = _selectedIds.value.toList()
            if (ids.isNotEmpty()) dao.deleteByIds(ids)
            clearSelection()
        }
    }

    val recentLanguages: StateFlow<List<Language>> = userPreferences.recentLanguages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun rememberLanguage(language: Language) {
        viewModelScope.launch {
            userPreferences.rememberLanguage(language)
        }
    }

    fun clearRecentLanguages() {
        viewModelScope.launch {
            userPreferences.clearRecentLanguages()
        }
    }

    fun save(
        id: String?,
        term: String,
        translation: String,
        pos: PartOfSpeech,
        languageCode: String,
        contexts: List<ContextSentence>,
        kind: EntryKind
    ) {
        viewModelScope.launch {
            val entryId = if (id.isNullOrEmpty()) {
                val entry = EntryEntity(
                    term = term.trim(),
                    translation = translation.trim(),
                    languageCode = languageCode,
                    createdAt = System.currentTimeMillis(),
                    partOfSpeech = pos,
                    kind = kind
                )
                dao.insert(entry)
                entry.id
            } else {
                val existing = dao.getById(id) ?: return@launch
                dao.update(
                    existing.copy(
                        term = term.trim(),
                        translation = translation.trim(),
                        languageCode = languageCode,
                        partOfSpeech = pos
                    )
                )
                id
            }
            val kept = contexts.filter { it.sentence.isNotBlank() }
            val existingIds = contextDao.getByEntryId(entryId).map { it.id }
            val kepIds = kept.map { it.id }.toSet()
            existingIds.filter { it !in kepIds }.forEach { contextDao.deleteById(it) }

            if (kept.isNotEmpty()) {
                contextDao.upsertAll(
                    kept.map { it.toEntity(entryId) }
                )
            }
        }
    }
}

class VocabularyViewModelFactory(
    private val dao: EntryDao,
    private val contextDao: ContextSentenceDao,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VocabularyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VocabularyViewModel(dao, contextDao, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
