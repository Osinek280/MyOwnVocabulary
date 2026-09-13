package com.example.myownvocabulary.ui.screens

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
import com.example.myownvocabulary.ui.components.BackButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddWordScreen(
    word: Word = Word(id = "", en = "", pl = "", partOfSpeech = PartOfSpeech.Noun),
    onBack: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val isNew = word.id.isEmpty()
    var en by remember { mutableStateOf(word.en) }
    var pl by remember { mutableStateOf(word.pl) }
    var pos by remember { mutableStateOf(word.partOfSpeech) }
    var contexts by remember { mutableStateOf(word.contexts) }
    var confirmDelete by remember { mutableStateOf(false) }
    var verbForms by remember {
        mutableStateOf(
            word.verbForms ?: VerbForms(
                infinitive = word.en,
                presentI = word.en,
                presentYou = word.en,
                presentHe = word.en + "s",
                presentWe = word.en,
                presentThey = word.en,
                pastSimple = "",
                pastParticiple = "",
                gerund = "",
            ),
        )
    }
    var addingContext by remember { mutableStateOf(false) }
    var contextDraft by remember { mutableStateOf("") }
    var pendingContext by remember { mutableStateOf<ContextSentence?>(null) }

    var expanded by remember { mutableStateOf(false) }

    fun handleSave() {
//        onSave(
//            word.copy(
//                en = en.trim().ifEmpty { word.en },
//                pl = pl.trim().ifEmpty { word.pl },
//                partOfSpeech = pos,
//                contexts = contexts,
//                verbForms = if (pos == PartOfSpeech.Verb) verbForms else null,
//            ),
//        )
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BackButton(onPress = onBack)
            Text(
                if (isNew) "Nowe słówko" else "Edytuj słówko",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primary)
                    .clickable { handleSave() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text("Zapisz", color = colors.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column {
                SectionLabel("Słówko")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = colors.outlineVariant)
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                    ) {
                        Text("ANGIELSKI", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.outline)
                        Spacer(Modifier.height(4.dp))
                        SimpleField(en, { en = it }, FontWeight.SemiBold, colors.onSurface)
                    }
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)) {
                        Text("POLSKI", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.outline)
                        Spacer(Modifier.height(4.dp))
                        SimpleField(pl, { pl = it }, FontWeight.Medium, colors.onSurface)
                    }
                }
            }
        }
    }
}


@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun SimpleField(
    value: String,
    onChange: (String) -> Unit,
    weight: FontWeight,
    color: Color,
) {
    val colors = MaterialTheme.colorScheme
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = weight, color = color),
        cursorBrush = SolidColor(colors.primary),
        modifier = Modifier.fillMaxWidth(),
    )
}