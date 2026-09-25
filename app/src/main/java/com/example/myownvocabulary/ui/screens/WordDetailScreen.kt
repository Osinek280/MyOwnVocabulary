package com.example.myownvocabulary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.data.context.TextSpan
import com.example.myownvocabulary.data.word.Language
import com.example.myownvocabulary.data.word.PartOfSpeech
import com.example.myownvocabulary.model.ContextSentence
import com.example.myownvocabulary.model.Word
import com.example.myownvocabulary.ui.components.AddContextCard
import com.example.myownvocabulary.ui.components.BackButton
import com.example.myownvocabulary.ui.components.ContextCard
import com.example.myownvocabulary.ui.components.ContextStep
import com.example.myownvocabulary.ui.components.ContextWizard
import com.example.myownvocabulary.ui.components.HighlightContextCard
import com.example.myownvocabulary.ui.components.LanguagePicker
import com.example.myownvocabulary.ui.components.PlusIcon
import com.example.myownvocabulary.ui.components.SectionLabel
import java.util.UUID

@Composable
fun WordDetailScreen(
    word: Word =
        Word(
            id = "",
            term = "",
            translation = "",
            languageCode = "en",
            partOfSpeech = PartOfSpeech.Noun,
            contexts = emptyList()
        ),
    words: List<Word>,
    onBack: () -> Unit = {},
    onSave: (String, String, PartOfSpeech, String, List<ContextSentence>) -> Unit,
    recentLanguages: List<Language> = emptyList(),
    onLanguageRemembered: (Language) -> Unit,
    onClearRecent: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isNew = word.id.isEmpty()

    var term by remember(word.id) { mutableStateOf(word.term) }
    var translation by remember(word.id) { mutableStateOf(word.translation) }
    var pos by remember(word.id) { mutableStateOf(word.partOfSpeech) }
    var language by remember(word.id) {
        mutableStateOf(
            if (isNew) {
                recentLanguages.firstOrNull() ?: Language.fromCode(word.languageCode)
            } else {
                Language.fromCode(word.languageCode)
            }
        )
    }

    var contexts by remember(word.id) { mutableStateOf(word.contexts) }
    var removedIds by remember(word.id) { mutableStateOf(emptySet<String>()) }
    val visibleContexts = contexts.filter { it.id !in removedIds }
    var editingId by remember(word.id) { mutableStateOf<String?>(null) }

    var step by remember { mutableStateOf(ContextStep.Idle) }
    var sentenceDraft by remember { mutableStateOf("") }
    var translationDraft by remember { mutableStateOf("") }
    var highlightsDraft by remember { mutableStateOf<List<TextSpan>>(emptyList()) }

    val normalizedTerm = term.trim()
    val normalizedTranslation = translation.trim()

    val termTaken = words.any {
        it.id != word.id &&
            it.languageCode == language.code &&
            it.term.equals(normalizedTerm, ignoreCase = true)
    }
    val translationTaken = words.any {
        it.id != word.id &&
            it.languageCode == language.code &&
            it.translation.equals(normalizedTranslation, ignoreCase = true)
    }

    val canSave = normalizedTerm.isNotEmpty() &&
        normalizedTranslation.isNotEmpty() &&
        !termTaken &&
        !translationTaken

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BackButton(onPress = onBack)
            Text(
                if (isNew) "Dodaj nowe wyrażenie" else "Edytuj wyrażenie",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (canSave) colors.primary else colors.surfaceVariant)
                    .clickable(enabled = canSave) {
                        onSave(term.trim(), translation.trim(), pos, language.code, contexts.filter { it.id !in removedIds })
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Zapisz",
                    color = if (canSave) colors.onPrimary else colors.outline,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("Język")
                LanguagePicker(
                    selected = language,
                    recent = recentLanguages,
                    onSelect = {
                        language = it
                        onLanguageRemembered(it)
                    },
                    onClearRecent = onClearRecent
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("Wyrażenie")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                    ) {
                        Text("Termin", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.outline)
                        Spacer(Modifier.height(4.dp))
                        SimpleField(
                            term,
                            { term = it.filter { c -> !c.isWhitespace() } },
                            FontWeight.SemiBold,
                            colors.onSurface
                        )
                        if (termTaken) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Takie wyrażenie już jest w tym języku",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.error.copy(alpha = 0.7f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = colors.outlineVariant
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                    ) {
                        Text("Tłumaczenie", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.outline)
                        Spacer(Modifier.height(4.dp))
                        SimpleField(translation, { translation = it }, FontWeight.Medium, colors.onSurface)
                        if (translationTaken) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Takie tłumaczenie już jest w tym języku",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.error.copy(alpha = 0.7f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Konteksty użycia")
                    if (removedIds.isNotEmpty()) {
                        Text(
                            "Przywróć wszystko",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary,
                            modifier = Modifier.clickable { removedIds = emptySet() }
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    visibleContexts.forEach { ctx ->
                        if (ctx.id == editingId) {
                            ContextWizard (
                                step = step,
                                sentenceDraft = sentenceDraft,
                                onSentenceDraftChange = { sentenceDraft = it },
                                translationDraft = translationDraft,
                                onTranslationDraftChange = { translationDraft = it },
                                highlightsDraft = highlightsDraft,
                                onToggleHighlight = { span ->
                                    highlightsDraft =
                                        if (highlightsDraft.any { it.start == span.start && it.end == span.end }) {
                                            highlightsDraft.filter { it.start != span.start || it.end != span.end }
                                        } else {
                                            highlightsDraft + span
                                        }
                                },
                                onStepChange = { step = it },
                                onConfirm = {
                                    contexts = contexts.map {
                                        if (it.id == ctx.id) {
                                            it.copy(
                                                sentence = sentenceDraft,
                                                translation = translationDraft,
                                                highlights = highlightsDraft
                                            )
                                        } else {
                                            it
                                        }
                                    }
                                    editingId = null
                                    step = ContextStep.Idle
                                },
                                onCancel = {
                                    editingId = null
                                    step = ContextStep.Idle
                                }
                            )
                        } else {
                            ContextCard(
                                ctx,
                                onEdit = {
                                    editingId = ctx.id
                                    sentenceDraft = ctx.sentence
                                    translationDraft = ctx.translation
                                    highlightsDraft = ctx.highlights
                                    step = ContextStep.Sentence
                                },
                                onRemove = {
                                    if (editingId == ctx.id) {
                                        editingId = null
                                        step = ContextStep.Idle
                                    }
                                    removedIds = removedIds + ctx.id
                                }
                            )
                        }
                    }

                    if (editingId == null) {
                        when (step) {
                            ContextStep.Idle -> {
                                if (visibleContexts.isEmpty()) {
                                    Text(
                                        "Nie masz dodanych jeszcze zadnych zdań kontektsowych, kliknij poniżej aby dodać",
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = colors.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .dashedBorder(2.dp, colors.outline, 16.dp)
                                        .clickable {
                                            sentenceDraft = ""
                                            translationDraft = ""
                                            highlightsDraft = emptyList()
                                            step = ContextStep.Sentence
                                        }
                                        .padding(vertical = 14.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    PlusIcon(color = colors.outline, iconSize = 16)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Dodaj kontekst",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }

                            else -> ContextWizard(
                                step = step,
                                sentenceDraft = sentenceDraft,
                                onSentenceDraftChange = { sentenceDraft = it },
                                translationDraft = translationDraft,
                                onTranslationDraftChange = { translationDraft = it },
                                highlightsDraft = highlightsDraft,
                                onToggleHighlight = { span ->
                                    highlightsDraft =
                                        if (highlightsDraft.any { it.start == span.start && it.end == span.end }) {
                                            highlightsDraft.filter { it.start != span.start || it.end != span.end }
                                        } else {
                                            highlightsDraft + span
                                        }
                                },
                                onStepChange = { step = it },
                                onConfirm = {
                                    contexts = contexts + ContextSentence(
                                        id = UUID.randomUUID().toString(),
                                        sentence = sentenceDraft,
                                        translation = translationDraft,
                                        highlights = highlightsDraft
                                    )
                                    step = ContextStep.Idle
                                },
                                onCancel = { step = ContextStep.Idle }
                            )
                        }
                    }
//                    contexts.forEach { ctx ->
//                        ContextCard(ctx, onRemove = { contexts = contexts.filter { it.id != ctx.id } })
//                    }
//                    when (step) {
//                        ContextStep.Idle -> {
//                            if (contexts.isEmpty()) {
//                                Text(
//                                    "Nie masz dodanych jeszcze zadnych zdań kontektsowych, kliknij poniżej aby dodać",
//                                    fontSize = 13.sp,
//                                    lineHeight = 18.sp,
//                                    color = colors.onSurfaceVariant,
//                                    textAlign = TextAlign.Center,
//                                    modifier = Modifier.fillMaxWidth()
//                                )
//                            }
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .dashedBorder(2.dp, colors.outline, 16.dp)
//                                    .clickable {
//                                        sentenceDraft = ""
//                                        translationDraft = ""
//                                        highlightsDraft = emptyList()
//                                        step = ContextStep.Sentence
//                                    }
//                                    .padding(vertical = 14.dp),
//                                horizontalArrangement = Arrangement.Center,
//                                verticalAlignment = Alignment.CenterVertically,
//                            ) {
//                                PlusIcon(color = colors.outline, iconSize = 16)
//                                Spacer(Modifier.width(8.dp))
//                                Text(
//                                    "Dodaj kontekst",
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = colors.onSurfaceVariant
//                                )
//                            }
//                        }
//
//                        ContextStep.Sentence -> AddContextCard(
//                            title = "Wpisz zdanie z kontekstem",
//                            placeholder = "np. Otwórz okno, jest duszno.",
//                            draft = sentenceDraft,
//                            onDraftChange = { sentenceDraft = it },
//                            onNext = {
//                                val sentence = sentenceDraft.trim()
//                                if (sentence.isNotEmpty()) {
//                                    sentenceDraft = sentence
//                                    step = ContextStep.Translation
//                                }
//                            },
//                            onCancel = { step = ContextStep.Idle }
//                        )
//
//                        ContextStep.Translation -> AddContextCard(
//                            title = "Wpisz tłumaczenie zdania",
//                            placeholder = "np. Open the window, it's stuffy.",
//                            draft = translationDraft,
//                            onDraftChange = { translationDraft = it },
//                            onNext = {
//                                val translation = translationDraft.trim()
//                                if (translation.isNotEmpty()) {
//                                    translationDraft = translation
//                                    step = ContextStep.Highlight
//                                }
//                            },
//                            onCancel = { step = ContextStep.Idle }
//                        )
//
//                        ContextStep.Highlight -> HighlightContextCard(
//                            sentence = sentenceDraft,
//                            translation = translationDraft,
//                            highlights = highlightsDraft,
//                            onToggle = { span ->
//                                highlightsDraft =
//                                    if (highlightsDraft.any { it.start == span.start && it.end == span.end }) {
//                                        highlightsDraft.filter { it.start != span.start || it.end != span.end }
//                                    } else {
//                                        highlightsDraft + span
//                                    }
//                            },
//                            onConfirm = {
//                                contexts = contexts + ContextSentence(
//                                    id = UUID.randomUUID().toString(),
//                                    sentence = sentenceDraft,
//                                    translation = translationDraft,
//                                    highlights = highlightsDraft
//                                )
//                                step = ContextStep.Idle
//                            },
//                            onCancel = { step = ContextStep.Idle }
//                        )
//                    }
                }
            }
        }
    }
}

private fun Modifier.dashedBorder(width: Dp, color: Color, cornerRadius: Dp): Modifier = drawBehind {
    val strokeWidth = width.toPx()
    val inset = strokeWidth / 2f
    val dash = strokeWidth * 3f
    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        cornerRadius = CornerRadius(cornerRadius.toPx() - inset),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash), 0f),
        ),
    )
}

@Composable
private fun SimpleField(value: String, onChange: (String) -> Unit, weight: FontWeight, color: Color) {
    val colors = MaterialTheme.colorScheme
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = weight, color = color),
        cursorBrush = SolidColor(colors.primary),
        modifier = Modifier.fillMaxWidth()
    )
}
