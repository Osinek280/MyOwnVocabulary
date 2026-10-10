package com.example.myownvocabulary.ui.components.learn

import com.example.myownvocabulary.data.entry.EntryKind

data class LearnDashboardState(
    val streakDays: Int,
    val dailyGoalDone: Int,
    val dailyGoal: Int,
    val dueCount: Int,
    val newCount: Int,
    val learningCount: Int,
    val masteredCount: Int,
    val sessionQuestionCount: Int,
    val estimatedMinutes: Int,
    val week: List<LearningDay>,
    val calendarMonths: List<LearningCalendarMonth>,
    val sessionSetup: LearnSessionSetupData
) {
    val dailyProgress: Float
        get() = if (dailyGoal <= 0) 0f else (dailyGoalDone.toFloat() / dailyGoal).coerceIn(0f, 1f)
}

data class LearningDay(val label: String, val completed: Boolean, val isToday: Boolean = false)

data class CalendarMonthId(val year: Int, val month: Int) {
    init {
        require(month in 1..12) { "Month must be in range 1..12." }
    }
}

data class LearningCalendarMonth(val id: CalendarMonthId, val completedDays: Set<Int>)

data class LearnFilterOption(val id: String, val label: String, val count: Int, val symbol: String? = null)

data class LearnEntryKindOption(val kind: EntryKind, val count: Int)

data class LearnSessionSetupData(
    val totalEntryCount: Int,
    val languages: List<LearnFilterOption>,
    val entryKinds: List<LearnEntryKindOption>,
    val tags: List<LearnFilterOption>
) {
    fun matchingCount(selection: LearnSessionSelection): Int {
        if (selection.includedLanguageIds.isEmpty() || selection.includedEntryKinds.isEmpty()) return 0

        val languageCount = languages
            .filter { it.id in selection.includedLanguageIds }
            .sumOf { it.count }
        val kindCount = entryKinds
            .filter { it.kind in selection.includedEntryKinds }
            .sumOf { it.count }
        val excludedCount = tags
            .filter { it.id in selection.excludedTagIds }
            .sumOf { it.count }

        return (minOf(languageCount, kindCount, totalEntryCount) - excludedCount)
            .coerceIn(0, totalEntryCount)
    }
}

data class LearnSessionSelection(
    val includedLanguageIds: Set<String>,
    val includedEntryKinds: Set<EntryKind>,
    val excludedTagIds: Set<String>
)
