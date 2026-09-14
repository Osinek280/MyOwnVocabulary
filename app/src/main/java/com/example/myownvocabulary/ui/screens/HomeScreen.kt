package com.example.myownvocabulary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.myownvocabulary.data.word.PartOfSpeech
import com.example.myownvocabulary.model.Word
import com.example.myownvocabulary.ui.components.ChevronRight
import com.example.myownvocabulary.ui.components.PlusIcon
import com.example.myownvocabulary.ui.components.SearchIcon
import com.example.myownvocabulary.ui.components.badgeLabel
import com.example.myownvocabulary.ui.components.posColor

val InitialWords: List<Word> = listOf(
    Word(
        id = "1",
        term = "citizen",
        translation = "obywatel / obywatelka",
        languageCode = "en",
        partOfSpeech = PartOfSpeech.Noun,
//        contexts = listOf(
//            ContextSentence("c1", "Every citizen has the right to vote.", 1),
//        ),
    ),
    Word(id = "2", term = "address", translation = "adres", languageCode = "en", partOfSpeech = PartOfSpeech.Noun),
    Word(
        id = "3",
        term = "travel",
        translation = "podróżować",
        languageCode = "en",
        partOfSpeech = PartOfSpeech.Verb,
//        contexts = listOf(
//            ContextSentence("c2", "She travels to Paris every year.", 1),
//        ),
//        verbForms = VerbForms(
//            infinitive = "travel",
//            presentI = "travel",
//            presentYou = "travel",
//            presentHe = "travels",
//            presentWe = "travel",
//            presentThey = "travel",
//            pastSimple = "travelled",
//            pastParticiple = "travelled",
//            gerund = "travelling",
//        ),
    ),
    Word(id = "4", term = "passport", translation = "paszport", languageCode = "en", partOfSpeech = PartOfSpeech.Noun),
    Word(id = "5", term = "identity", translation = "tożsamość", languageCode = "en", partOfSpeech = PartOfSpeech.Noun),
    Word(
        id = "6",
        term = "speak",
        translation = "mówić",
        partOfSpeech = PartOfSpeech.Verb,
        languageCode = "en",
//        contexts = listOf(
//            ContextSentence("c3", "She spoke fluent French at the meeting.", 1),
//        ),
//        verbForms = VerbForms(
//            infinitive = "speak",
//            presentI = "speak",
//            presentYou = "speak",
//            presentHe = "speaks",
//            presentWe = "speak",
//            presentThey = "speak",
//            pastSimple = "spoke",
//            pastParticiple = "spoken",
//            gerund = "speaking",
//        ),
    ),
    Word(id = "7", term = "freedom", translation = "wolność", languageCode = "en", partOfSpeech = PartOfSpeech.Noun),
    Word(id = "8", term = "law", translation = "prawo", languageCode = "en", partOfSpeech = PartOfSpeech.Noun),
    Word(
        id = "9",
        term = "choose",
        translation = "wybierać",
        partOfSpeech = PartOfSpeech.Verb,
        languageCode = "en",
//        verbForms = VerbForms(
//            infinitive = "choose",
//            presentI = "choose",
//            presentYou = "choose",
//            presentHe = "chooses",
//            presentWe = "choose",
//            presentThey = "choose",
//            pastSimple = "chose",
//            pastParticiple = "chosen",
//            gerund = "choosing",
//        ),
    ),
    Word(id = "10", term = "democracy", translation = "demokracja", languageCode = "en", partOfSpeech = PartOfSpeech.Noun),
)

@Composable
fun HomeScreen(
    words: List<Word>,
    isLoading: Boolean,
    onAddClick: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    var search by remember { mutableStateOf("") }
    val filtered = words.filter {
        it.term.contains(search, ignoreCase = true) || it.translation.contains(search, ignoreCase = true)
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "MyOwnVocabulary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground,
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center,
                ) {
                    PlusIcon()
                }
            }
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
                    value = search,
                    onValueChange = { search = it },
                    singleLine = true,
                    textStyle = searchStyle,
                    cursorBrush = SolidColor(colors.primary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (search.isEmpty()) {
                                Text(
                                    text = "Szukaj słówka...",
                                    style = searchStyle.copy(color = colors.outline),
                                    maxLines = 1,
                                )
                            }
                            inner()
                        }
                    },
                )
            }
        }
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            words.isEmpty() -> {
                EmptyVocabularyState(onAddClick = onAddClick)
            }

            filtered.isEmpty() -> {
                EmptySearchState(query = search)
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Text(
                            "${filtered.size} słówek",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.outline,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(filtered, key = { it.id }) { word ->
                        WordRow(word)
                    }
                }
            }
        }
    }
}

@Composable
private fun WordRow(word: Word) {
    val colors = MaterialTheme.colorScheme
    val badgeColor = posColor(word.partOfSpeech)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(word.term, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
            Text(
                word.partOfSpeech.badgeLabel(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = badgeColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(badgeColor.copy(alpha = 0.09f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(word.translation, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.outline)
            ChevronRight()
        }
    }
}

@Composable
private fun EmptyVocabularyState(onAddClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Brak słówek",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Dodaj pierwsze słówko, żeby zacząć naukę.",
            fontSize = 14.sp,
            color = colors.outline,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAddClick) {
            Text("Dodaj słówko")
        }
    }
}

@Composable
private fun EmptySearchState(query: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Nic nie znaleziono",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Brak wyników dla „$query”.",
            fontSize = 14.sp,
            color = colors.outline,
        )
    }
}