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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myownvocabulary.data.word.Language
import com.example.myownvocabulary.data.word.PartOfSpeech
import com.example.myownvocabulary.model.Word
import com.example.myownvocabulary.ui.components.BackButton
import com.example.myownvocabulary.ui.components.LanguagePicker
import com.example.myownvocabulary.ui.components.SectionLabel

@Composable
fun WordDetailScreen(
    word: Word = Word(id = "", term = "", translation = "", languageCode = "en", partOfSpeech = PartOfSpeech.Noun),
    words: List<Word>,
    onBack: () -> Unit = {},
    onSave: (String, String, PartOfSpeech, String) -> Unit,
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
                        onSave(term.trim(), translation.trim(), pos, language.code)
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
        }
    }
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
