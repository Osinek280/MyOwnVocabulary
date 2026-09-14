package com.example.myownvocabulary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.data.word.Language

@Composable
fun LanguagePicker(
    selected: Language,
    onSelect: (Language) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var showSheet by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .clickable { showSheet = true }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            selected.displayName,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
        )
        Box(modifier = Modifier.rotate(90f)) {
            ChevronRight()
        }
    }

    if (showSheet) {
        LanguagePickerSheet(
            selected = selected,
            onSelect = onSelect,
            onDismiss = { showSheet = false },
        )
    }
}

private val ConsumeSheetScroll = object : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset = Offset(x = 0f, y = available.y)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePickerSheet(
    selected: Language,
    onSelect: (Language) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val sheetHeight = (LocalConfiguration.current.screenHeightDp.dp - 48.dp).coerceAtLeast(200.dp)
    val languages = remember { Language.entries.sortedBy { it.displayName } }
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, languages) {
        if (query.isBlank()) languages
        else languages.filter { it.displayName.contains(query, ignoreCase = true) }
    }
    val onLanguageClick = remember(onSelect, onDismiss) {
        { language: Language ->
            onSelect(language)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Text(
                "Wybierz język",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                modifier = Modifier.padding(bottom = 14.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SearchIcon()
                val searchStyle = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = colors.onSurface,
                )
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = searchStyle,
                    cursorBrush = SolidColor(colors.primary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Szukaj języka...",
                                    style = searchStyle.copy(color = colors.outline),
                                    maxLines = 1,
                                )
                            }
                            inner()
                        }
                    },
                )
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(ConsumeSheetScroll),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (filtered.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            "Brak wyników",
                            fontSize = 14.sp,
                            color = colors.outline,
                            modifier = Modifier.padding(vertical = 20.dp),
                        )
                    }
                } else {
                    items(
                        items = filtered,
                        key = { it.code },
                        contentType = { "language" },
                    ) { option ->
                        LanguageOptionRow(
                            option = option,
                            selected = option == selected,
                            onSelect = onLanguageClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageOptionRow(
    option: Language,
    selected: Boolean,
    onSelect: (Language) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(option) }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            option.displayName,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) colors.primary else colors.onSurface,
        )
        Text(
            option.code.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.outline,
        )
    }
}
