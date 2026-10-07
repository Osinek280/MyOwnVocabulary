package com.example.myownvocabulary.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.Language
import com.example.myownvocabulary.model.Entry
import com.example.myownvocabulary.ui.components.EntryRow
import com.example.myownvocabulary.ui.components.PlusIcon
import com.example.myownvocabulary.ui.components.SearchBar
import com.example.myownvocabulary.ui.components.states.EmptySearchState
import com.example.myownvocabulary.ui.components.states.EmptyVocabularyState
import com.example.myownvocabulary.ui.components.states.LoadingState
import com.example.myownvocabulary.ui.text.ExpressionNoun
import com.example.myownvocabulary.ui.text.PolishNumeralCase

@Composable
fun HomeScreen(
    entries: List<Entry>,
    isLoading: Boolean,
    onAddClick: (EntryKind) -> Unit = {},
    onEntryClick: (String) -> Unit = {},
    onToggleSelect: (String) -> Unit,
    onEnterSelection: (String) -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    selectedIds: Set<String> = emptySet()
) {
    val colors = MaterialTheme.colorScheme
    var search by remember { mutableStateOf("") }

    var languageFilter by remember { mutableStateOf<Language?>(null) }
    var kindFilter by remember { mutableStateOf<EntryKind?>(null) }

    val languages = remember(entries) {
        entries.map { Language.fromCode(it.languageCode) }
            .distinctBy { it.code }
            .sortedBy { it.displayName }
    }
    val activeLanguage = languageFilter?.takeIf { selected ->
        languages.any { it.code == selected.code }
    }

    val filtered = entries.filter { entry ->
        val matchesSearch = entry.term.contains(search, ignoreCase = true) ||
            entry.translation.contains(search, ignoreCase = true)
        val matchesLanguage = activeLanguage == null || entry.languageCode == activeLanguage.code
        val matchesKind = kindFilter == null || entry.kind == kindFilter
        matchesSearch && matchesLanguage && matchesKind
    }

    var pendingDelete by remember { mutableStateOf(false) }

    var addMenuOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedIds.isNotEmpty()) {
        onClearSelection()
    }

    if (pendingDelete) {
        AlertDialog(
            onDismissRequest = { pendingDelete = false },
            title = { Text("Usunąć wyrażenia?") },
            text = { Text(deleteExpressionsConfirmation(selectedIds.size)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = false
                    onDeleteSelected()
                }) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = false }) { Text("Anuluj") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "MyOwnVocabulary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground
                )
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .clickable { addMenuOpen = true },
                        contentAlignment = Alignment.Center
                    ) {
                        PlusIcon()
                    }
                    DropdownMenu(
                        expanded = addMenuOpen,
                        onDismissRequest = { addMenuOpen = false },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        EntryKind.entries.forEach { kind ->
                            DropdownMenuItem(
                                text = { Text("${kind.icon} ${kind.singularLabel}") },
                                onClick = {
                                    addMenuOpen = false
                                    onAddClick(kind)
                                }
                            )
                        }
                    }
                }
            }
            SearchBar(
                value = search,
                onValueChange = { search = it },
                placeholder = "Szukaj wyrażenia..."
            )
        }
        when {
            isLoading -> {
                LoadingState(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            entries.isEmpty() -> {
                EmptyVocabularyState(onAddClick = { onAddClick(EntryKind.Word) })
            }

            else -> {
                val inSelection = selectedIds.isNotEmpty()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            ExpressionNoun.withCount(filtered.size),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.outline
                        )
                        if (inSelection) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SelectionChip(
                                    label = "Anuluj",
                                    onClick = onClearSelection,
                                    containerColor = colors.surfaceVariant,
                                    contentColor = colors.onSurface
                                )
                                SelectionChip(
                                    label = "Usuń",
                                    onClick = { pendingDelete = true },
                                    containerColor = colors.error,
                                    contentColor = colors.onError
                                )
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterMenu(
                                    label =
                                    activeLanguage?.let { "${it.flagEmoji} ${it.displayName}" }
                                        ?: "Wszystkie języki",
                                    options = listOf(null) + languages,
                                    optionLabel = { language ->
                                        language?.let { "${it.flagEmoji} ${it.displayName}" } ?: "Wszystkie języki"
                                    },
                                    onSelect = { selected ->
                                        languageFilter = selected
                                        onClearSelection()
                                    }
                                )
                                FilterMenu(
                                    label = kindFilter?.let { "${it.icon} ${it.pluralLabel}" } ?: "Wszystkie typy",
                                    options = listOf<EntryKind?>(null) + EntryKind.entries,
                                    optionLabel = { kind ->
                                        kind?.let { "${it.icon} ${it.pluralLabel}" } ?: "Wszystkie"
                                    },
                                    onSelect = { selected ->
                                        kindFilter = selected
                                        onClearSelection()
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (filtered.isEmpty()) {
                        EmptySearchState(
                            query = search,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { entry ->
                                EntryRow(
                                    entry = entry,
                                    isSelectionMode = inSelection,
                                    isSelected = entry.id in selectedIds,
                                    onClick = {
                                        if (inSelection) {
                                            onToggleSelect(entry.id)
                                        } else {
                                            onEntryClick(entry.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (inSelection) {
                                            onToggleSelect(entry.id)
                                        } else {
                                            onEnterSelection(entry.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> FilterMenu(label: String, options: List<T>, optionLabel: (T) -> String, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Box {
        SelectionChip(
            label = label,
            onClick = { expanded = true },
            containerColor = colors.surfaceVariant,
            contentColor = colors.onSurface
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

private fun deleteExpressionsConfirmation(count: Int): String {
    val counted = ExpressionNoun.withCount(count)
    return when (PolishNumeralCase.forCount(count)) {
        PolishNumeralCase.NominativeSingular -> "Zaznaczone wyrażenie zostanie usunięte."
        PolishNumeralCase.NominativePlural -> "Zaznaczone $counted zostaną usunięte."
        PolishNumeralCase.GenitivePlural -> "Zaznaczonych $counted zostanie usuniętych."
    }
}

@Composable
private fun SelectionChip(label: String, onClick: () -> Unit, containerColor: Color, contentColor: Color) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
