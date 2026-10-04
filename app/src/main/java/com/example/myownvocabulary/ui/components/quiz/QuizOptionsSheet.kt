package com.example.myownvocabulary.ui.components.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizOptionsSheet(
    options: QuizOptions,
    onChange: (QuizOptions) -> Unit,
    onDismiss: () -> Unit,
    onRestart: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var showQuestionTypeError by rememberSaveable { mutableStateOf(false) }
    var questionTypeErrorAttempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(showQuestionTypeError) {
        if (showQuestionTypeError) {
            delay(3_000)
            showQuestionTypeError = false
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp
        ),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Text(
            text = "Opcje",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp, bottom = 24.dp)
        )
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OptionSection("Ogólne") {
                OptionToggle("Potasuj pojęcia", options.shuffle) { onChange(options.copy(shuffle = it)) }
                // OptionToggle("Syntezator mowy", options.speech) { onChange(options.copy(speech = it)) }
            }
            OptionSection("Odpowiedź") {
                OptionToggle(
                    "Pojęcie",
                    options.answerWithTerm
                ) { onChange(options.copy(answerWithTerm = it)) }
                OptionToggle(
                    "Definicja",
                    options.answerWithDefinition
                ) { onChange(options.copy(answerWithDefinition = it)) }
            }
            OptionSection("Typy pytań") {
                /*
                OptionToggle(
                    "Fiszki", options.flashcards
                ) {
                    if (it || options.multipleChoice || options.written) {
                        showQuestionTypeError = false
                        onChange(options.copy(flashcards = it))
                    } else {
                        questionTypeErrorAttempt++
                        showQuestionTypeError = true
                    }
                }
                 */
                OptionToggle(
                    "Wielokrotny wybór",
                    options.multipleChoice
                ) {
                    if (
                        it || options.written
                        // || options.flashcards
                    ) {
                        showQuestionTypeError = false
                        onChange(options.copy(multipleChoice = it))
                    } else {
                        questionTypeErrorAttempt++
                        showQuestionTypeError = true
                    }
                }
                OptionToggle(
                    "Pisemny",
                    options.written
                ) {
                    if (
                        it || options.multipleChoice
                        // || options.flashcards
                    ) {
                        showQuestionTypeError = false
                        onChange(options.copy(written = it))
                    } else {
                        questionTypeErrorAttempt++
                        showQuestionTypeError = true
                    }
                }
                if (showQuestionTypeError) {
                    Text(
                        text = "Musisz aktywować co najmniej jeden rodzaj pytania",
                        color = colors.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }
            OptionSection("Ocenianie pytań pisemnych") {
                /*
                Column(
                    Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WrittenGrading.entries.forEach { grading ->
                        GradingOption(
                            grading = grading,
                            selected = options.grading == grading,
                            onClick = { onChange(options.copy(grading = grading)) }
                        )
                    }
                }
                HorizontalDivider(color = colors.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                 */
                OptionToggle(
                    "Wpisz ponownie poprawne odpowiedzi",
                    options.retypeCorrectAnswer
                ) { onChange(options.copy(retypeCorrectAnswer = it)) }
                Text(
                    "Jeśli odpowiesz błędnie na pytanie pisemne, zostanie wyświetlona prawidłowa odpowiedź, " +
                        "którą będziesz musiał przepisać przed przejściem do następnego pytania.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceVariant)
                    .clickable(onClick = onRestart)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Uruchom ponownie tryb Ucz się",
                    color = colors.error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "To zrestartuje dotychczasowy progres.",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
//                Text(
//                    "Uruchom ponownie tryb Ucz się – to zrestartuje dotychczasowy progres.",
//                    color = colors.error,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
            }
        }
    }
}

@Composable
private fun OptionSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun OptionToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp)
            .toggleable(
                value = checked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
                onValueChange = onChange
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}

// @Composable
// private fun GradingOption(
//    grading: WrittenGrading,
//    selected: Boolean,
//    onClick: () -> Unit
// ) {
//    val colors = MaterialTheme.colorScheme
//    val color = colors.onSurfaceVariant
//    val backgroundColor by animateColorAsState(
//        targetValue = if (selected) colors.primary.copy(alpha = 0.08f) else colors.surfaceVariant,
//        label = "Grading selection background"
//    )
//    Column(
//        Modifier.fillMaxWidth()
//            .clip(RoundedCornerShape(14.dp))
//            .background(backgroundColor)
//            .selectable(
//                selected = selected,
//                interactionSource = remember { MutableInteractionSource() },
//                indication = ripple(color = colors.primary),
//                role = Role.RadioButton,
//                onClick = onClick
//            )
//            .padding(horizontal = 12.dp, vertical = 12.dp)
//    ) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Text(
//                grading.label,
//                modifier = Modifier.weight(1f),
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.SemiBold,
//                color = color
//            )
//            RadioButton(selected = selected, onClick = null)
//        }
//        Text(grading.description, style = MaterialTheme.typography.bodyMedium, color = color)
//    }
// }
