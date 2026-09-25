package com.example.myownvocabulary.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.data.context.TextSpan
import com.example.myownvocabulary.model.ContextSentence

@Composable
fun ContextCard(ctx: ContextSentence, onRemove: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = highlightedSentence(ctx.sentence, ctx.highlights, colors.primary),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface
            )
            if (ctx.translation.isNotBlank()) {
                Text(
                    text = ctx.translation,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurfaceVariant
                )
            }
        }
        Text(
            "Usuń",
            fontSize = 12.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            color = colors.outline,
            modifier = Modifier.clickable(onClick = onRemove)
        )
    }
}

private fun highlightedSentence(sentence: String, highlights: List<TextSpan>, color: Color): AnnotatedString =
    buildAnnotatedString {
        append(sentence)
        highlights.forEach { span ->
            val start = span.start.coerceIn(0, sentence.length)
            val end = span.end.coerceIn(start, sentence.length)
            addStyle(
                SpanStyle(color = color, fontWeight = FontWeight.SemiBold),
                start,
                end
            )
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddContextCard(
    title: String,
    placeholder: String,
    draft: String,
    onDraftChange: (String) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(imeBottom) {
        if (imeBottom > 0) bringIntoViewRequester.bringIntoView()
    }

    Column(
        modifier = Modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .border(2.dp, colors.primary, RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.primary)
        Spacer(Modifier.height(8.dp))
        val fieldStyle = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = colors.onSurface
        )
        BasicTextField(
            value = draft,
            onValueChange = onDraftChange,
            textStyle = fieldStyle,
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (draft.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = fieldStyle.copy(color = colors.outline)
                        )
                    }
                    inner()
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val ready = draft.trim().isNotEmpty()
            PrimaryAction(
                "Dalej →",
                if (ready) colors.primary else colors.outline,
                colors.onPrimary,
                Modifier.weight(1f)
            ) { onNext() }
            PrimaryAction("Anuluj", colors.surfaceVariant, colors.onSurfaceVariant, onClick = onCancel)
        }
    }
}

@Composable
private fun PrimaryAction(
    label: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HighlightContextCard(
    sentence: String,
    translation: String,
    highlights: List<TextSpan>,
    onToggle: (TextSpan) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val ready = highlights.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, colors.primary, RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            "Dotknij formy słowa w zdaniu:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (translation.isNotBlank()) {
            Text(
                translation,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sentenceTokens(sentence).forEach { (token, span) ->
                val selected = highlights.any { it.start == span.start && it.end == span.end }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) colors.primary else colors.primaryContainer)
                        .clickable { onToggle(span) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        token,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) colors.onPrimary else colors.onSurface
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryAction(
                if (ready) "Potwierdź" else "Wybierz formę",
                if (ready) colors.tertiary else colors.outline,
                if (ready) colors.onTertiary else colors.onPrimary,
                Modifier.weight(1f)
            ) { if (ready) onConfirm() }
            PrimaryAction("Anuluj", colors.surfaceVariant, colors.onSurfaceVariant, onClick = onCancel)
        }
    }
}

private fun sentenceTokens(sentence: String): List<Pair<String, TextSpan>> {
    val tokens = mutableListOf<Pair<String, TextSpan>>()
    var i = 0
    while (i < sentence.length) {
        while (i < sentence.length && sentence[i].isWhitespace()) i++
        val start = i
        while (i < sentence.length && !sentence[i].isWhitespace()) i++
        if (start < i) tokens += sentence.substring(start, i) to TextSpan(start, i)
    }
    return tokens
}
