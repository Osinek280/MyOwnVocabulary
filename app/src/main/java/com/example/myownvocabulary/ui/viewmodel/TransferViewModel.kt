package com.example.myownvocabulary.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.myownvocabulary.data.AppDatabase
import com.example.myownvocabulary.data.context.ContextSentenceDao
import com.example.myownvocabulary.data.entry.EntryDao
import com.example.myownvocabulary.data.transfer.FileEntry
import com.example.myownvocabulary.data.transfer.VocabularyCodec
import com.example.myownvocabulary.data.transfer.VocabularyFormatException
import com.example.myownvocabulary.data.transfer.toEntity
import com.example.myownvocabulary.data.transfer.toFileEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface TransferResult {
    data class Exported(val count: Int) : TransferResult
    data class Imported(val added: Int, val updated: Int, val skipped: Int) : TransferResult
}

data class TransferUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val result: TransferResult? = null,
    val importPreview: List<FileEntry>? = null,
    val importSkipped: Int = 0
)

class TransferViewModel(
    private val database: AppDatabase,
    private val dao: EntryDao,
    private val contextDao: ContextSentenceDao
) : ViewModel() {
    private val _transfer = MutableStateFlow(TransferUiState())
    val transfer: StateFlow<TransferUiState> = _transfer.asStateFlow()

    private var stagedExportIds: List<String> = emptyList()
    private var importGeneration = 0

    fun stageExport(ids: List<String>) {
        stagedExportIds = ids
    }

    fun dismissTransferError() {
        _transfer.update { it.copy(error = null) }
    }

    fun acknowledgeTransferFeedback() {
        _transfer.update {
            it.copy(
                error = null,
                result = null,
                importPreview = null,
                importSkipped = 0
            )
        }
    }

    fun discardImport() {
        importGeneration++
        _transfer.update {
            it.copy(
                busy = false,
                importPreview = null,
                importSkipped = 0
            )
        }
    }

    fun beginImport(uri: Uri, resolver: ContentResolver) {
        val generation = ++importGeneration
        _transfer.value = TransferUiState(busy = true)
        viewModelScope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { stream ->
                        stream.readBytes().decodeToString()
                    }
                } ?: throw VocabularyFormatException("Nie udało się odczytać pliku.")
                val decoded = VocabularyCodec.decode(text)
                if (generation != importGeneration) return@launch
                if (decoded.entries.isEmpty()) {
                    val message = if (decoded.skipped > 0) {
                        "Żaden wpis w pliku nie jest poprawny."
                    } else {
                        "Plik nie zawiera wpisów."
                    }
                    _transfer.update { it.copy(busy = false, error = message) }
                } else {
                    _transfer.update {
                        it.copy(
                            busy = false,
                            importPreview = decoded.entries,
                            importSkipped = decoded.skipped
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: VocabularyFormatException) {
                if (generation != importGeneration) return@launch
                _transfer.update {
                    it.copy(busy = false, error = error.message ?: "Nie udało się odczytać pliku.")
                }
            } catch (error: Exception) {
                if (generation != importGeneration) return@launch
                _transfer.update { it.copy(busy = false, error = "Nie udało się odczytać pliku.") }
            }
        }
    }

    fun exportStaged(uri: Uri, resolver: ContentResolver) {
        if (_transfer.value.busy) return
        val ids = stagedExportIds
        if (ids.isEmpty()) {
            _transfer.update { it.copy(error = "Nie wybrano wpisów do eksportu.") }
            return
        }
        _transfer.update { it.copy(busy = true, error = null, result = null) }
        viewModelScope.launch {
            try {
                val byId = ids.chunked(ID_CHUNK).flatMap { dao.getByIds(it) }.associateBy { it.id }
                val contexts = ids.chunked(ID_CHUNK)
                    .flatMap { contextDao.getByEntryIds(it) }
                    .groupBy { it.entryId }
                val fileEntries = ids.mapNotNull { id ->
                    val entry = byId[id] ?: return@mapNotNull null
                    entry.toFileEntry(contexts[id].orEmpty())
                }
                val payload = VocabularyCodec.encode(fileEntries)
                withContext(Dispatchers.IO) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        stream.write(payload.toByteArray(Charsets.UTF_8))
                    } ?: throw IllegalStateException("Brak strumienia zapisu")
                }
                _transfer.update {
                    it.copy(
                        busy = false,
                        result = TransferResult.Exported(fileEntries.size)
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _transfer.update { it.copy(busy = false, error = "Nie udało się zapisać pliku.") }
            }
        }
    }

    fun importSelected(ids: List<String>) {
        if (_transfer.value.busy) return
        val preview = _transfer.value.importPreview.orEmpty()
        val chosenIds = ids.toSet()
        val chosen = preview.filter { it.id in chosenIds }
        if (chosen.isEmpty()) {
            _transfer.update { it.copy(error = "Nie wybrano wpisów do importu.") }
            return
        }
        val skipped = _transfer.value.importSkipped
        _transfer.update { it.copy(busy = true, error = null, result = null) }
        viewModelScope.launch {
            try {
                val outcome = database.withTransaction {
                    var added = 0
                    var updated = 0
                    for (fileEntry in chosen) {
                        val entity = fileEntry.toEntity()
                        val existing = dao.getById(entity.id)
                        if (existing == null) {
                            dao.insert(entity)
                            added++
                        } else {
                            dao.update(entity)
                            updated++
                        }
                        contextDao.deleteByEntryId(entity.id)
                        val contexts = fileEntry.contexts.map { it.toEntity(entity.id) }
                        if (contexts.isNotEmpty()) contextDao.upsertAll(contexts)
                    }
                    added to updated
                }
                _transfer.update {
                    it.copy(
                        busy = false,
                        result = TransferResult.Imported(
                            added = outcome.first,
                            updated = outcome.second,
                            skipped = skipped
                        )
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _transfer.update { it.copy(busy = false, error = "Nie udało się zaimportować wpisów.") }
            }
        }
    }

    private companion object {
        const val ID_CHUNK = 500
    }
}

class TransferViewModelFactory(
    private val database: AppDatabase,
    private val dao: EntryDao,
    private val contextDao: ContextSentenceDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransferViewModel(database, dao, contextDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
