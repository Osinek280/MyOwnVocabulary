package com.example.myownvocabulary.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.Language
import com.example.myownvocabulary.data.transfer.TransferPreview
import com.example.myownvocabulary.data.transfer.TransferScope
import com.example.myownvocabulary.data.transfer.TransferSelection
import com.example.myownvocabulary.data.transfer.matches
import com.example.myownvocabulary.ui.components.BackButton
import com.example.myownvocabulary.ui.components.SectionLabel
import com.example.myownvocabulary.ui.components.badgeLabel
import com.example.myownvocabulary.ui.components.posColor
import com.example.myownvocabulary.ui.components.states.LoadingState

@Composable
fun TransferScreen(
    title: String,
    confirmLabel: String,
    rows: List<TransferPreview>,
    existingIds: Set<String>,
    showImportStatus: Boolean,
    isLoading: Boolean,
    busy: Boolean,
    error: String?,
    onBack: () -> Unit,
    onConfirm: (List<String>) -> Unit,
    onDismissError: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var selection by remember { mutableStateOf(TransferSelection()) }
    var excludedIds by rememberSaveable(
        stateSaver = listSaver<Set<String>, String>(
            save = { it.toList() },
            restore = { it.toSet() }
        )
    ) { mutableStateOf(emptySet<String>()) }

    val languageOptions = remember(rows) { languageOptions(rows) }
    val kindOptions = remember(rows) { kindOptions(rows) }
    val narrowed = rows.filter { selection.matches(it.languageCode, it.kind) }
    val chosen = narrowed.filter { it.id !in excludedIds }
    val canConfirm = chosen.isNotEmpty() && !busy && !isLoading

    if (error != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Nie udało się") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onDismissError) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onPress = onBack)
            Text(
                title,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
            Spacer(Modifier.size(24.dp))
        }

        if (isLoading) {
            LoadingState(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                ScopeSwitch(
                    scope = selection.scope,
                    onSelect = { selection = selection.copy(scope = it) }
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "filters") {
                    AnimatedVisibility(
                        visible = selection.scope == TransferScope.Filtered,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        FilterSections(
                            languageOptions = languageOptions,
                            kindOptions = kindOptions,
                            selection = selection,
                            onToggleLanguage = { code ->
                                val next = if (code in selection.languageCodes) {
                                    selection.languageCodes - code
                                } else {
                                    selection.languageCodes + code
                                }
                                selection = selection.copy(languageCodes = next)
                            },
                            onToggleKind = { kind ->
                                val next = if (kind in selection.kinds) {
                                    selection.kinds - kind
                                } else {
                                    selection.kinds + kind
                                }
                                selection = selection.copy(kinds = next)
                            }
                        )
                    }
                }
                item(key = "summary") {
                    Column {
                        Text(
                            when {
                                narrowed.isEmpty() -> "Brak wpisów dla wybranych filtrów."
                                else -> summaryLabel(narrowed)
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.outline
                        )
                        Text(
                            "Zaznaczono: ${chosen.size} z ${narrowed.size}",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant
                        )
                        Row {
                            TextButton(
                                enabled = !busy && chosen.size < narrowed.size,
                                onClick = { excludedIds = excludedIds - narrowed.map { it.id }.toSet() }
                            ) { Text("Zaznacz wszystkie") }
                            TextButton(
                                enabled = !busy && chosen.isNotEmpty(),
                                onClick = { excludedIds = excludedIds + narrowed.map { it.id } }
                            ) { Text("Odznacz wszystkie") }
                        }
                    }
                }
                items(narrowed, key = { it.id }) { row ->
                    PreviewRow(
                        row = row,
                        selected = row.id !in excludedIds,
                        enabled = !busy,
                        onToggle = {
                            excludedIds = if (row.id in excludedIds) {
                                excludedIds - row.id
                            } else {
                                excludedIds + row.id
                            }
                        },
                        status = if (!showImportStatus) {
                            null
                        } else if (row.id in existingIds) {
                            "zastąpi"
                        } else {
                            "nowy"
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (canConfirm || busy) colors.primary else colors.surfaceVariant)
                .clickable(enabled = canConfirm) {
                    onConfirm(chosen.map { it.id })
                },
            contentAlignment = Alignment.Center
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = colors.onPrimary
                )
            } else {
                Text(
                    confirmLabel,
                    color = if (canConfirm) colors.onPrimary else colors.outline,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ScopeSwitch(scope: TransferScope, onSelect: (TransferScope) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .padding(4.dp)
    ) {
        ScopeSegment(
            label = "Wszystkie",
            selected = scope == TransferScope.All,
            onClick = { onSelect(TransferScope.All) },
            modifier = Modifier.weight(1f)
        )
        ScopeSegment(
            label = "Wybrane",
            selected = scope == TransferScope.Filtered,
            onClick = { onSelect(TransferScope.Filtered) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScopeSegment(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) colors.onPrimary else colors.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSections(
    languageOptions: List<LanguageOption>,
    kindOptions: List<KindOption>,
    selection: TransferSelection,
    onToggleLanguage: (String) -> Unit,
    onToggleKind: (EntryKind) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (languageOptions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Język")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(null) + languageOptions.forEach { option ->
                        SelectChip(
                            label = "${option.label} · ${option.count}",
                            selected = option.code in selection.languageCodes,
                            onClick = { onToggleLanguage(option.code) }
                        )
                    }
                }
            }
        }
        if (kindOptions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Typ")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    kindOptions.forEach { option ->
                        SelectChip(
                            label = "${option.kind.icon.trim()} ${option.kind.pluralLabel} · ${option.count}",
                            selected = option.kind in selection.kinds,
                            onClick = { onToggleKind(option.kind) }
                        )
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Tagi")
            Text(
                "Wkrótce będzie można filtrować po tagach, np. #school",
                fontSize = 13.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun SelectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(if (selected) colors.primary else colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) colors.onPrimary else colors.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PreviewRow(
    row: TransferPreview,
    selected: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    status: String?
) {
    val colors = MaterialTheme.colorScheme
    val knownLanguage = Language.entries.find { it.code.equals(row.languageCode, ignoreCase = true) }
    val badgeColor = posColor(row.partOfSpeech)
    val isNew = status == "nowy"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .toggleable(
                value = selected,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = { onToggle() }
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(knownLanguage?.flagEmoji ?: row.languageCode, fontSize = 12.sp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    row.term,
                    modifier = Modifier.weight(1f, fill = false),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(row.kind.icon.trim(), fontSize = 12.sp)
                Text(
                    row.partOfSpeech.badgeLabel(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(badgeColor.copy(alpha = 0.09f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text(
                row.translation,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (status != null) {
            Text(
                status,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isNew) colors.onTertiaryContainer else colors.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (isNew) colors.tertiaryContainer else colors.surface)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Checkbox(checked = selected, onCheckedChange = null, enabled = enabled)
    }
}

private data class LanguageOption(val code: String, val label: String, val count: Int)

private data class KindOption(val kind: EntryKind, val count: Int)

private fun languageOptions(rows: List<TransferPreview>): List<LanguageOption> =
    rows.groupingBy { it.languageCode }.eachCount()
        .map { (code, count) ->
            val known = Language.entries.find { it.code.equals(code, ignoreCase = true) }
            val label = if (known != null) "${known.flagEmoji} ${known.displayName}" else code
            LanguageOption(code, label, count)
        }
        .sortedBy { it.label }

private fun kindOptions(rows: List<TransferPreview>): List<KindOption> = rows.groupingBy { it.kind }.eachCount()
    .map { (kind, count) -> KindOption(kind, count) }
    .sortedBy { it.kind.ordinal }

private fun summaryLabel(rows: List<TransferPreview>): String = rows.groupBy { it.kind }
    .toSortedMap(compareBy { it.ordinal })
    .entries
    .joinToString(", ") { (kind, items) ->
        val label = if (items.size == 1) kind.singularLabel else kind.pluralLabel
        "${items.size} ${label.replaceFirstChar { it.lowercase() }}"
    }
