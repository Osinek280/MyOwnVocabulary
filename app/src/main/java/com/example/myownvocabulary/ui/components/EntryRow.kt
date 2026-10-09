package com.example.myownvocabulary.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.data.entry.Language
import com.example.myownvocabulary.model.Entry

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntryRow(
    entry: Entry,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val accent = when (entry.kind) {
        EntryKind.Word -> colors.outline
        EntryKind.Expression -> colors.tertiary
        EntryKind.Idiom -> colors.primary
        EntryKind.Sentence -> colors.secondary
        EntryKind.Numeral -> colors.tertiary
    }
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                when {
                    isSelected -> colors.primary.copy(alpha = 0.12f)
                    entry.kind == EntryKind.Word -> colors.surfaceVariant
                    else -> accent.copy(alpha = 0.06f)
                }
            )
            .then(
                if (entry.kind == EntryKind.Idiom) {
                    Modifier.border(1.dp, accent.copy(alpha = 0.2f), shape)
                } else {
                    Modifier
                }
            )
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(
                        value = isSelected,
                        enabled = enabled,
                        role = Role.Checkbox,
                        onValueChange = onCheckedChange
                    )
                } else {
                    Modifier.combinedClickable(
                        enabled = enabled,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val language = Language.entries.find { it.code.equals(entry.languageCode, ignoreCase = true) }
        Text(language?.flagEmoji ?: entry.languageCode, fontSize = 12.sp)
        Box(Modifier.weight(1f)) {
            when (entry.kind) {
                EntryKind.Word -> WordRow(entry)
                EntryKind.Expression -> ExpressionRow(entry, accent)
                EntryKind.Idiom -> IdiomRow(entry, accent)
                EntryKind.Sentence -> SentenceRow(entry, accent)
                EntryKind.Numeral -> NumeralRow(entry, accent)
            }
        }
        if (trailingContent != null) {
            trailingContent()
        } else if (isSelected || isSelectionMode) {
            SelectionCheckbox(checked = isSelected, color = colors.primary)
        } else if (!isSelectionMode) {
            ChevronRight()
        }
    }
}

@Composable
private fun WordRow(entry: Entry) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            entry.term,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            entry.partOfSpeech?.let { partOfSpeech ->
                EntryBadge(partOfSpeech.badgeLabel(), posColor(partOfSpeech))
            }
            Text(
                entry.translation,
                modifier = Modifier.weight(1f, fill = false),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpressionRow(entry: Entry, accent: Color) {
    StackedEntryRow(entry, accent)
}

@Composable
private fun IdiomRow(entry: Entry, accent: Color) {
    StackedEntryRow(entry, accent, italic = true)
}

@Composable
private fun SentenceRow(entry: Entry, accent: Color) {
    StackedEntryRow(entry, accent)
}

@Composable
private fun NumeralRow(entry: Entry, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            entry.term,
            modifier = Modifier.weight(1f),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.End
        ) {
            EntryBadge(entry.kind.singularLabel, accent)
            EntryTranslation(entry.translation)
        }
    }
}

@Composable
private fun StackedEntryRow(entry: Entry, accent: Color, italic: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (italic) "\"${entry.term}\"" else entry.term,
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (entry.kind == EntryKind.Sentence) FontWeight.Normal else FontWeight.SemiBold,
                    fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                entry.translation,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                color = MaterialTheme.colorScheme.outline
            )
        }
        EntryBadge(entry.kind.singularLabel, accent)
    }
}

@Composable
private fun EntryTranslation(translation: String) {
    Text(translation, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
}

@Composable
private fun EntryBadge(label: String, color: Color) {
    Text(
        label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(color.copy(alpha = 0.09f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
