package com.example.myownvocabulary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myownvocabulary.data.word.PartOfSpeech
import com.example.myownvocabulary.data.word.WordDao
import com.example.myownvocabulary.data.word.WordEntity
import com.example.myownvocabulary.model.Word
import com.example.myownvocabulary.model.toWord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WordsUiState(
    val words: List<Word> = emptyList(),
    val isLoading: Boolean = true,
)

class VocabularyViewModel(private val dao: WordDao) : ViewModel() {
    val uiState: StateFlow<WordsUiState> = dao.observeAll()
        .map {
            list -> WordsUiState(
                words = list.map { it.toWord() },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WordsUiState(isLoading = true),
        )

    fun save(term: String, translation: String, pos: PartOfSpeech, languageCode: String) {
        viewModelScope.launch {
            dao.insert(
                WordEntity(
                    term = term.trim(),
                    translation = translation.trim(),
                    languageCode = languageCode,
                    createdAt = System.currentTimeMillis(),
                    partOfSpeech = pos,
                )
            )
        }
    }

    fun delete(word: Word) {
        viewModelScope.launch {
            dao.getById(word.id)?.let { dao.delete(it) }
        }
    }
}

class VocabularyViewModelFactory(private val dao: WordDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VocabularyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VocabularyViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}