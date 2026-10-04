package com.example.myownvocabulary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myownvocabulary.ui.components.quiz.QuizOptions
import com.example.myownvocabulary.ui.components.quiz.QuizOptionsSaver
import com.example.myownvocabulary.ui.components.quiz.QuizOptionsSheet
import com.example.myownvocabulary.ui.components.quiz.QuizProgressBar

@Composable
fun QuizScreen(
    onBack: () -> Unit = {},
    onOptionsChanged: (QuizOptions) -> Unit = {},
    onRestart: (QuizOptions) -> Unit = {},
    isError: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    var optionMenuOpen by rememberSaveable { mutableStateOf(false) }
    var options by rememberSaveable(stateSaver = QuizOptionsSaver) { mutableStateOf(QuizOptions()) }

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
            currentIndex = 1,
            totalCount = 28,
            isError = isError,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
        )
    }

    if (optionMenuOpen) {
        QuizOptionsSheet(
            options = options,
            onChange = {
                options = it
                onOptionsChanged(it)
            },
            onDismiss = { optionMenuOpen = false },
            onRestart = {
                optionMenuOpen = false
                onRestart(options)
            }
        )
    }
}
