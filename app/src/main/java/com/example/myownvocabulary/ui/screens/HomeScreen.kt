package com.example.myownvocabulary.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.Language
import com.example.myownvocabulary.data.prefs.HomeOptions
import com.example.myownvocabulary.data.prefs.HomeSort
import com.example.myownvocabulary.data.prefs.sortEntries
import com.example.myownvocabulary.model.Entry
import com.example.myownvocabulary.ui.components.EntryRow
import com.example.myownvocabulary.ui.components.PlusIcon
import com.example.myownvocabulary.ui.components.SearchBar
import com.example.myownvocabulary.ui.components.SelectionCheckbox
import com.example.myownvocabulary.ui.components.states.EmptySearchState
import com.example.myownvocabulary.ui.components.states.EmptyVocabularyState
import com.example.myownvocabulary.ui.components.states.LoadingState
import com.example.myownvocabulary.ui.text.ExpressionNoun
import com.example.myownvocabulary.ui.text.PolishNumeralCase

@Composable
fun HomeScreen(
    entries: List<Entry>,
    options: HomeOptions,
    onOptionsChange: (HomeOptions) -> Unit,
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
    var search by rememberSaveable { mutableStateOf("") }

    val languageFilter = options.languages.toList()
    val kindFilter = options.kinds.toList()

    val languages = remember(entries) {
        entries.map { Language.fromCode(it.languageCode) }
            .distinctBy { it.code }
            .sortedBy { it.displayName }
    }
    val activeLanguages = languages.filter { it.code in languageFilter }
    val activeKinds = EntryKind.entries.filter { it.name in kindFilter }
    val allLanguagesSelected = languageFilter.isEmpty()
    val allKindsSelected = kindFilter.isEmpty()

    val filtered = entries.filter { entry ->
        val matchesSearch = entry.term.contains(search, ignoreCase = true) ||
            entry.translation.contains(search, ignoreCase = true)
        val matchesLanguage = allLanguagesSelected || entry.languageCode in languageFilter
        val matchesKind = allKindsSelected || entry.kind.name in kindFilter
        matchesSearch && matchesLanguage && matchesKind
    }.let { options.sort.sortEntries(it) }

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
                            SortMenu(options.sort) { onOptionsChange(options.copy(sort = it)) }
                        }
                    }
                    if (!inSelection) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterMenu(
                                label = when {
                                    allLanguagesSelected -> "Wszystkie języki"
                                    activeLanguages.size == 1 -> activeLanguages.single().let {
                                        "${it.flagEmoji} ${it.displayName}"
                                    }
                                    else -> "Języki (${languageFilter.size})"
                                },
                                allLabel = "Wszystkie języki",
                                allSelected = allLanguagesSelected,
                                onSelectAll = {
                                    onOptionsChange(options.copy(languages = emptySet()))
                                    onClearSelection()
                                },
                                options = languages,
                                optionLabel = { language ->
                                    "${language.flagEmoji} ${language.displayName}"
                                },
                                isSelected = { !allLanguagesSelected && it.code in languageFilter },
                                onSelect = { selected ->
                                    onOptionsChange(
                                        options.copy(
                                            languages = toggleFilter(
                                                selected = if (allLanguagesSelected) emptyList() else languageFilter,
                                                value = selected.code,
                                                options = languages.map { it.code }
                                            ).toSet()
                                        )
                                    )
                                    onClearSelection()
                                }
                            )
                            FilterMenu(
                                label = when {
                                    allKindsSelected -> "Wszystkie typy"
                                    activeKinds.size == 1 -> activeKinds.single().let {
                                        "${it.icon} ${it.pluralLabel}"
                                    }
                                    else -> "Typy (${kindFilter.size})"
                                },
                                allLabel = "Wszystkie typy",
                                allSelected = allKindsSelected,
                                onSelectAll = {
                                    onOptionsChange(options.copy(kinds = emptySet()))
                                    onClearSelection()
                                },
                                options = EntryKind.entries,
                                optionLabel = { kind ->
                                    "${kind.icon} ${kind.pluralLabel}"
                                },
                                isSelected = { !allKindsSelected && it.name in kindFilter },
                                onSelect = { selected ->
                                    onOptionsChange(
                                        options.copy(
                                            kinds = toggleFilter(
                                                selected = if (allKindsSelected) emptyList() else kindFilter,
                                                value = selected.name,
                                                options = EntryKind.entries.map { it.name }
                                            ).toSet()
                                        )
                                    )
                                    onClearSelection()
                                }
                            )
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
private fun <T> FilterMenu(
    label: String,
    allLabel: String,
    allSelected: Boolean,
    onSelectAll: () -> Unit,
    options: List<T>,
    optionLabel: (T) -> String,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit
) {
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
            DropdownMenuItem(
                modifier = Modifier.semantics {
                    role = Role.Checkbox
                    toggleableState = ToggleableState(allSelected)
                },
                text = { Text(allLabel) },
                trailingIcon = { SelectionCheckbox(checked = allSelected) },
                onClick = onSelectAll
            )
            options.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.semantics {
                        role = Role.Checkbox
                        toggleableState = ToggleableState(isSelected(option))
                    },
                    text = { Text(optionLabel(option)) },
                    trailingIcon = { SelectionCheckbox(checked = isSelected(option)) },
                    onClick = {
                        onSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun SortMenu(sort: HomeSort, onSelect: (HomeSort) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        SelectionChip(
            label = sort.label + " \u25be",
            onClick = { expanded = true },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            HomeSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    trailingIcon = { if (option == sort) Text("\u2713") },
                    modifier = Modifier.semantics {
                        role = Role.RadioButton
                        selected = option == sort
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun toggleFilter(selected: List<String>, value: String, options: List<String>): List<String> {
    val updated = if (value in selected) selected - value else selected + value
    return if (updated.containsAll(options)) emptyList() else updated
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
