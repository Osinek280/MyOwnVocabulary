package com.example.myownvocabulary.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myownvocabulary.ui.components.quiz.QuizOptions
import com.example.myownvocabulary.ui.components.quiz.QuizOptionsSaver
import com.example.myownvocabulary.ui.components.quiz.QuizOptionsSheet
import com.example.myownvocabulary.ui.components.quiz.QuizProgressBar
import com.example.myownvocabulary.ui.navigation.QuizQuestion
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(
    currentIndex: Int,
    totalCount: Int,
    question: QuizQuestion,
    onBack: () -> Unit,
    onNextQuestion: () -> Unit,
    onRestart: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var optionMenuOpen by rememberSaveable { mutableStateOf(false) }
    var quizOptions by rememberSaveable(stateSaver = QuizOptionsSaver) { mutableStateOf(QuizOptions()) }
    var selectedAnswer by rememberSaveable(currentIndex, question) {
        mutableStateOf<String?>(null)
    }
    var answerRevealed by rememberSaveable(currentIndex, question) { mutableStateOf(false) }
    val correct = !answerRevealed &&
        selectedAnswer?.trim()?.equals(question.correctAnswer.trim(), ignoreCase = true) == true
    val incorrect = selectedAnswer != null && !correct
    val nextQuestion by rememberUpdatedState(onNextQuestion)
    LaunchedEffect(currentIndex, question, selectedAnswer, answerRevealed) {
        if (correct) {
            delay(650)
            nextQuestion()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Zamknij", tint = colors.onBackground)
            }
            IconButton(onClick = { optionMenuOpen = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Opcje quizu", tint = colors.onBackground)
            }
        }
        QuizProgressBar(
            currentIndex,
            totalCount,
            isError = incorrect,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
        )
        AnimatedContent(
            targetState = currentIndex to question,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            transitionSpec = {
                (slideInHorizontally { it / 5 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 5 } + fadeOut())
            },
            contentAlignment = Alignment.TopCenter,
            label = "Quiz question"
        ) { (index, displayedQuestion) ->
            ChooseAnswerContent(
                question = displayedQuestion,
                currentIndex = index,
                selectedAnswer = if (index == currentIndex) selectedAnswer else null,
                answerRevealed = index == currentIndex && answerRevealed,
                onAnswer = { answer ->
                    if (index == currentIndex && selectedAnswer == null && !answerRevealed) {
                        selectedAnswer = answer
                    }
                },
                onRevealAnswer = {
                    if (index == currentIndex && selectedAnswer == null) answerRevealed = true
                },
                modifier = Modifier.fillMaxHeight()
            )
        }
        if (incorrect || answerRevealed) {
            Button(
                onClick = onNextQuestion,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .heightIn(min = 48.dp)
            ) {
                Text("Kontynuuj")
            }
        }
    }

    if (optionMenuOpen) {
        QuizOptionsSheet(
            options = quizOptions,
            onChange = {
                quizOptions = it
            },
            onDismiss = { optionMenuOpen = false },
            onRestart = {
                optionMenuOpen = false
                selectedAnswer = null
                answerRevealed = false
                onRestart()
            }
        )
    }
}

@Composable
private fun ChooseAnswerContent(
    question: QuizQuestion,
    currentIndex: Int,
    selectedAnswer: String?,
    answerRevealed: Boolean,
    onAnswer: (String) -> Unit,
    onRevealAnswer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var typedAnswer by rememberSaveable(currentIndex, question) { mutableStateOf("") }
    Column(
        modifier = modifier
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        Text(
            text = question.prompt,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = if (question.options.isEmpty()) "Wpisz odpowiedź" else "Wybierz odpowiedź",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        if (question.options.isEmpty()) {
            if (answerRevealed) {
                AnswerOptionCard(
                    text = question.correctAnswer,
                    onClick = {},
                    borderColor = colors.tertiary,
                    dashed = true,
                    enabled = false
                )
            } else if (selectedAnswer == null) {
                OutlinedTextField(
                    value = typedAnswer,
                    onValueChange = { typedAnswer = it },
                    singleLine = true,
                    label = { Text("Odpowiedź") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onAnswer(typedAnswer) },
                    enabled = typedAnswer.isNotBlank(),
                    modifier = Modifier.padding(top = 12.dp)
                ) { Text("Sprawdź") }
            } else if (!selectedAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)) {
                AnswerOptionCard(
                    text = typedAnswer,
                    onClick = { },
                    borderColor = colors.error,
                    dashed = false,
                    enabled = false
                )
                Spacer(Modifier.height(12.dp))
                AnswerOptionCard(
                    text = question.correctAnswer,
                    onClick = { },
                    borderColor = colors.tertiary,
                    dashed = true,
                    enabled = false
                )
            } else {
                AnswerOptionCard(
                    text = typedAnswer,
                    onClick = { },
                    borderColor = colors.tertiary,
                    dashed = false,
                    enabled = false
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            question.options.forEach { option ->
                val borderColor = when {
                    answerRevealed && option == question.correctAnswer -> colors.tertiary
                    selectedAnswer == null -> colors.outlineVariant
                    option == question.correctAnswer -> colors.tertiary
                    option == selectedAnswer -> colors.error
                    else -> colors.outlineVariant
                }
                AnswerOptionCard(
                    text = option,
                    onClick = { onAnswer(option) },
                    borderColor = borderColor,
                    dashed = option == question.correctAnswer &&
                        (answerRevealed || (selectedAnswer != null && selectedAnswer != question.correctAnswer)),
                    enabled = selectedAnswer == null && !answerRevealed
                )
            }
        }
        if (selectedAnswer == null && !answerRevealed) {
            Spacer(Modifier.height(8.dp))
            DontKnowLink(
                onClick = onRevealAnswer,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun DontKnowLink(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = "Nie znasz odpowiedzi?",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    )
}

@Composable
private fun AnswerOptionCard(
    text: String,
    onClick: () -> Unit,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    dashed: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .then(
                if (dashed) {
                    Modifier.dashedBorder(2.dp, borderColor, 16.dp)
                } else {
                    Modifier.border(BorderStroke(2.dp, borderColor), shape)
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (borderColor) {
                MaterialTheme.colorScheme.tertiary -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Poprawna odpowiedź",
                    tint = borderColor,
                    modifier = Modifier.size(24.dp)
                )
                MaterialTheme.colorScheme.error -> Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Błędna odpowiedź",
                    tint = borderColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
