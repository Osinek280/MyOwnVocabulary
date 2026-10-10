package com.example.myownvocabulary.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.model.EntryTag
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

internal val EntryTagsSaver = listSaver<List<EntryTag>, String>(
    save = { tags -> tags.flatMap { listOf(it.id, it.label, it.hue.toString()) } },
    restore = { values -> values.chunked(3).map { EntryTag(id = it[0], label = it[1], hue = it[2].toFloat()) } }
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntryTags(entryId: String, tags: List<EntryTag>, onTagsChange: (List<EntryTag>) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var sectionSize by remember { mutableStateOf(IntSize.Zero) }
    val sectionResponder = remember {
        object : BringIntoViewResponder {
            override fun calculateRectForParent(localRect: Rect): Rect =
                Rect(0f, 0f, sectionSize.width.toFloat(), sectionSize.height.toFloat())

            override suspend fun bringChildIntoView(localRect: () -> Rect?) = Unit
        }
    }
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    var isFocused by remember(entryId) { mutableStateOf(false) }
    LaunchedEffect(isFocused, imeInsets, density) {
        if (!isFocused) return@LaunchedEffect

        bringIntoViewRequester.bringIntoView()
        snapshotFlow { imeInsets.getBottom(density) }
            .distinctUntilChanged()
            .filter { it > 0 }
            .conflate()
            .collect { bringIntoViewRequester.bringIntoView() }
    }
    var draft by rememberSaveable(entryId) { mutableStateOf("") }
    var nextColor by rememberSaveable(entryId) { mutableStateOf(tags.size) }
    val name = draft.filterNot { it.isWhitespace() }.trimStart('#')
    val duplicate = tags.any { it.label.equals(name, ignoreCase = true) }
    val canAdd = name.isNotEmpty() && !duplicate
    val addTag = {
        if (canAdd) {
            var hue = (235f + nextColor * 137.508f) % 360f
            while (tags.any { kotlin.math.abs(it.hue - hue) < 0.01f }) {
                nextColor++
                hue = (235f + nextColor * 137.508f) % 360f
            }
            onTagsChange(tags + EntryTag(label = name, hue = hue))
            nextColor++
            draft = ""
        }
    }

    Column(
        modifier = Modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .onSizeChanged { sectionSize = it }
            .bringIntoViewResponder(sectionResponder)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SectionLabel("Tagi")
        Column(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceVariant)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tags.forEach { tag ->
                        val foreground = Color.hsv(tag.hue, 0.75f, 0.55f)
                        Row(
                            modifier = Modifier.height(32.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.hsv(tag.hue, 0.14f, 1f))
                                .border(1.dp, foreground.copy(alpha = 0.12f), RoundedCornerShape(50))
                                .padding(start = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "#${tag.label}",
                                maxLines = 1,
                                softWrap = false,
                                color = foreground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier.padding(start = 4.dp).size(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable(role = Role.Button, onClickLabel = "Usuń tag ${tag.label}") {
                                        onTagsChange(tags.filterNot { it.id == tag.id })
                                    }
                                    .semantics { contentDescription = "Usuń tag ${tag.label}" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("×", color = foreground, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it.filterNot { character -> character.isWhitespace() } },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.onSurface, fontSize = 14.sp, lineHeight = 20.sp),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addTag() }),
                    modifier = Modifier.weight(1f)
                        .height(48.dp)
                        .onFocusChanged { isFocused = it.isFocused }
                        .semantics { contentDescription = "Nowy tag" }
                        .onPreviewKeyEvent {
                            if (it.key == Key.Enter || it.key == Key.NumPadEnter) {
                                if (it.type == KeyEventType.KeyUp) addTag()
                                true
                            } else {
                                false
                            }
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    decorationBox = { innerTextField ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                            if (draft.isEmpty()) {
                                Text(
                                    "# np. podróże",
                                    style = TextStyle(color = colors.outline, fontSize = 14.sp, lineHeight = 20.sp),
                                    maxLines = 1
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                IconButton(
                    onClick = { addTag() },
                    enabled = canAdd,
                    modifier = Modifier.size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canAdd) colors.primary else colors.outlineVariant)
                        .semantics { contentDescription = "Dodaj tag" }
                ) {
                    PlusIcon(color = if (canAdd) colors.onPrimary else colors.surface, iconSize = 18)
                }
            }
            Text(
                if (duplicate) "Ten tag jest już dodany" else "Dodaj tag Enterem lub przyciskiem plus.",
                color = if (duplicate) colors.error else colors.outline,
                fontSize = 11.sp
            )
        }
    }
}
