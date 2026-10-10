package com.example.myownvocabulary.ui.components.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myownvocabulary.data.entry.EntryKind

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LearnSessionSetupSheet(
    data: LearnSessionSetupData,
    questionLimit: Int,
    onDismiss: () -> Unit,
    onStart: (LearnSessionSelection) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var languageIds by rememberSaveable(data) {
        mutableStateOf(data.languages.map { it.id })
    }
    var kindIds by rememberSaveable(data) {
        mutableStateOf(data.entryKinds.map { it.kind.name })
    }
    var excludedTagIds by rememberSaveable(data) {
        mutableStateOf<List<String>>(emptyList())
    }
    val selection = LearnSessionSelection(
        includedLanguageIds = languageIds.toSet(),
        includedEntryKinds = kindIds.map(EntryKind::valueOf).toSet(),
        excludedTagIds = excludedTagIds.toSet()
    )
    val matchingCount = data.matchingCount(selection)
    val sessionSize = minOf(questionLimit, matchingCount)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Wybierz zakres nauki",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zdecyduj, które wpisy mają znaleźć się w tej sesji.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SetupSection(
                title = "Języki",
                supportingText = selectedSummary(languageIds.size, data.languages.size)
            ) {
                OptionGrid(
                    options = data.languages,
                    selectedIds = languageIds.toSet(),
                    onToggle = { languageIds = toggleId(languageIds, it) }
                )
            }

            SetupSection(
                title = "Typy wpisów",
                supportingText = selectedSummary(kindIds.size, data.entryKinds.size)
            ) {
                OptionGrid(
                    options = data.entryKinds.map { option ->
                        LearnFilterOption(
                            id = option.kind.name,
                            label = option.kind.pluralLabel,
                            count = option.count,
                            symbol = option.kind.icon
                        )
                    },
                    selectedIds = kindIds.toSet(),
                    onToggle = { kindIds = toggleId(kindIds, it) }
                )
            }

            SetupSection(
                title = "Pomiń tagi",
                supportingText = if (excludedTagIds.isEmpty()) {
                    "Żadne wpisy nie są pomijane"
                } else {
                    "Pomijasz ${excludedTagIds.size}"
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.tags.forEach { tag ->
                        val excluded = tag.id in excludedTagIds
                        FilterChip(
                            selected = excluded,
                            onClick = { excludedTagIds = toggleId(excludedTagIds, tag.id) },
                            label = { Text(tag.label) },
                            leadingIcon = if (excluded) {
                                {
                                    Text(
                                        text = "−",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                null
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "$matchingCount pasujących wpisów",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (matchingCount == 0) {
                                    "Zmień filtry, aby rozpocząć"
                                } else {
                                    "Sesja wykorzysta maksymalnie $questionLimit"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$sessionSize",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { onStart(selection) },
                        enabled = matchingCount > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp)
                    ) {
                        Text(
                            text = "Rozpocznij sesję",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupSection(title: String, supportingText: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

@Composable
private fun OptionGrid(options: List<LearnFilterOption>, selectedIds: Set<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowOptions.forEach { option ->
                    SelectableOptionCard(
                        option = option,
                        selected = option.id in selectedIds,
                        onClick = { onToggle(option.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowOptions.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SelectableOptionCard(
    option: LearnFilterOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) colors.primaryContainer else colors.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            option.symbol?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${option.count}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.primary else colors.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private fun toggleId(ids: List<String>, id: String): List<String> = if (id in ids) ids - id else ids + id

private fun selectedSummary(selected: Int, total: Int): String =
    if (selected == total) "Wszystkie" else "$selected z $total"
