package com.example.myownvocabulary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myownvocabulary.data.prefs.UserPreferences
import com.example.myownvocabulary.data.word.Language
import com.example.myownvocabulary.data.word.PartOfSpeech
import com.example.myownvocabulary.data.word.WordDao
import com.example.myownvocabulary.data.word.WordEntity
import com.example.myownvocabulary.model.Word
import com.example.myownvocabulary.model.toWord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordsUiState(val words: List<Word> = emptyList(), val isLoading: Boolean = true)

class VocabularyViewModel(private val dao: WordDao, private val userPreferences: UserPreferences) : ViewModel() {
    val uiState: StateFlow<WordsUiState> = dao.observeAll()
        .map { list ->
            WordsUiState(
                words = list.map { it.toWord() },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WordsUiState(isLoading = true)
        )

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    val isSelectionMode: Boolean
        get() = _selectedIds.value.isNotEmpty()

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

    fun save(id: String?, term: String, translation: String, pos: PartOfSpeech, languageCode: String) {
        viewModelScope.launch {
            if (id.isNullOrEmpty()) {
                dao.insert(
                    WordEntity(
                        term = term.trim(),
                        translation = translation.trim(),
                        languageCode = languageCode,
                        createdAt = System.currentTimeMillis(),
                        partOfSpeech = pos
                    )
                )
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
            }
        }
    }
}

class VocabularyViewModelFactory(private val dao: WordDao, private val userPreferences: UserPreferences) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VocabularyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VocabularyViewModel(dao, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
