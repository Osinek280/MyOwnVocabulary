package com.example.myownvocabulary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myownvocabulary.ui.components.learn.DailySessionCard
import com.example.myownvocabulary.ui.components.learn.LearnDashboardHeader
import com.example.myownvocabulary.ui.components.learn.LearnDashboardState
import com.example.myownvocabulary.ui.components.learn.LearnSessionSelection
import com.example.myownvocabulary.ui.components.learn.LearnSessionSetupSheet
import com.example.myownvocabulary.ui.components.learn.LearningCalendarSheet
import com.example.myownvocabulary.ui.components.learn.LearningModesSection
import com.example.myownvocabulary.ui.components.learn.VocabularyProgressCard
import com.example.myownvocabulary.ui.components.learn.WeekActivityCard
import com.example.myownvocabulary.ui.preview.LearnDashboardPreviewData
import com.example.myownvocabulary.ui.theme.MyOwnVocabularyTheme

@Composable
fun HomeScreen(
    state: LearnDashboardState = LearnDashboardPreviewData.state,
    onStartDailySession: (LearnSessionSelection) -> Unit = {},
    onQuickReview: () -> Unit = {},
    onConsolidate: () -> Unit = {},
    onReviewNew: () -> Unit = {},
    onOpenVocabulary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var calendarOpen by rememberSaveable { mutableStateOf(false) }
    var sessionSetupOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(2.dp))
        LearnDashboardHeader(streakDays = state.streakDays)
        DailySessionCard(
            dueCount = state.dueCount,
            newCount = state.newCount,
            questionCount = state.sessionQuestionCount,
            estimatedMinutes = state.estimatedMinutes,
            dailyGoalDone = state.dailyGoalDone,
            dailyGoal = state.dailyGoal,
            progress = state.dailyProgress,
            onStart = { sessionSetupOpen = true }
        )
        WeekActivityCard(
            days = state.week,
            onOpenCalendar = { calendarOpen = true }
        )
        LearningModesSection(
            newCount = state.newCount,
            onQuickReview = onQuickReview,
            onConsolidate = onConsolidate,
            onReviewNew = onReviewNew
        )
        VocabularyProgressCard(
            masteredCount = state.masteredCount,
            learningCount = state.learningCount,
            newCount = state.newCount,
            onClick = onOpenVocabulary
        )
        Spacer(Modifier.height(8.dp))
    }

    if (calendarOpen) {
        LearningCalendarSheet(
            months = state.calendarMonths,
            onDismiss = { calendarOpen = false }
        )
    }
    if (sessionSetupOpen) {
        LearnSessionSetupSheet(
            data = state.sessionSetup,
            questionLimit = state.sessionQuestionCount,
            onDismiss = { sessionSetupOpen = false },
            onStart = { selection ->
                sessionSetupOpen = false
                onStartDailySession(selection)
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun LearnDashboardScreenPreview() {
    MyOwnVocabularyTheme(darkTheme = false) {
        HomeScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun LearnDashboardScreenDarkPreview() {
    MyOwnVocabularyTheme(darkTheme = true) {
        HomeScreen()
    }
}
