package com.example.myownvocabulary.ui.preview

import com.example.myownvocabulary.data.entry.EntryKind
import com.example.myownvocabulary.ui.components.learn.CalendarMonthId
import com.example.myownvocabulary.ui.components.learn.LearnDashboardState
import com.example.myownvocabulary.ui.components.learn.LearnEntryKindOption
import com.example.myownvocabulary.ui.components.learn.LearnFilterOption
import com.example.myownvocabulary.ui.components.learn.LearnSessionSetupData
import com.example.myownvocabulary.ui.components.learn.LearningCalendarMonth
import com.example.myownvocabulary.ui.components.learn.LearningDay

object LearnDashboardPreviewData {
    val state = LearnDashboardState(
        streakDays = 6,
        dailyGoalDone = 7,
        dailyGoal = 10,
        dueCount = 12,
        newCount = 4,
        learningCount = 18,
        masteredCount = 84,
        sessionQuestionCount = 10,
        estimatedMinutes = 3,
        week = listOf(
            LearningDay("Pn", completed = true),
            LearningDay("Wt", completed = true),
            LearningDay("Śr", completed = true),
            LearningDay("Cz", completed = true),
            LearningDay("Pt", completed = true),
            LearningDay("So", completed = true, isToday = true),
            LearningDay("Nd", completed = false)
        ),
        calendarMonths = listOf(
            LearningCalendarMonth(
                id = CalendarMonthId(year = 2026, month = 9),
                completedDays = setOf(1, 2, 4, 5, 7, 8, 9, 11, 12, 14, 15, 18, 19, 21, 22, 24, 25, 28, 29)
            ),
            LearningCalendarMonth(
                id = CalendarMonthId(year = 2026, month = 10),
                completedDays = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            ),
            LearningCalendarMonth(
                id = CalendarMonthId(year = 2026, month = 11),
                completedDays = emptySet()
            )
        ),
        sessionSetup = LearnSessionSetupData(
            totalEntryCount = 106,
            languages = listOf(
                LearnFilterOption("en", "Angielski", 72, "🇬🇧"),
                LearnFilterOption("es", "Hiszpański", 22, "🇪🇸"),
                LearnFilterOption("de", "Niemiecki", 12, "🇩🇪")
            ),
            entryKinds = listOf(
                LearnEntryKindOption(EntryKind.Word, 60),
                LearnEntryKindOption(EntryKind.Expression, 14),
                LearnEntryKindOption(EntryKind.Idiom, 12),
                LearnEntryKindOption(EntryKind.Sentence, 10),
                LearnEntryKindOption(EntryKind.Numeral, 10)
            ),
            tags = listOf(
                LearnFilterOption("work", "Praca", 14),
                LearnFilterOption("travel", "Podróże", 19),
                LearnFilterOption("easy", "Łatwe", 31),
                LearnFilterOption("archived", "Archiwalne", 8)
            )
        )
    )
}
