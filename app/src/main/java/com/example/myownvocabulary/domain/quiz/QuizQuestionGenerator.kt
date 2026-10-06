package com.example.myownvocabulary.domain.quiz

import com.example.myownvocabulary.model.Entry
import com.example.myownvocabulary.ui.components.quiz.QuizOptions
import kotlin.random.Random

/** Builds questions from entries; scheduling and review progress belong outside this mapper. */
object QuizQuestionGenerator {
    fun generate(entries: List<Entry>, quizOptions: QuizOptions, random: Random = Random.Default): List<QuizQuestion> {
        require(quizOptions.written || quizOptions.multipleChoice) {
            "At least one quiz mode must be enabled."
        }
        return entries.map { entry ->
            val requestedMode = when {
                !quizOptions.multipleChoice -> QuizMode.TYPE
                !quizOptions.written -> QuizMode.CHOOSE
                random.nextBoolean() -> QuizMode.CHOOSE
                else -> QuizMode.TYPE
            }
            val distractors = if (requestedMode == QuizMode.CHOOSE) {
                entries
                    .filter {
                        it.languageCode == entry.languageCode &&
                            it.kind == entry.kind &&
                            !it.term.trim().equals(entry.term.trim(), ignoreCase = true) &&
                            !it.translation.trim().equals(entry.translation.trim(), ignoreCase = true)
                    }
                    .map { it.term.trim() }
                    .distinctBy { it.lowercase() }
                    .shuffled(random)
                    .take(3)
            } else {
                emptyList()
            }
            val mode = if (requestedMode == QuizMode.CHOOSE && distractors.size != 3) {
                QuizMode.TYPE
            } else {
                requestedMode
            }

            QuizQuestion(
                prompt = entry.translation.trim(),
                correctAnswer = entry.term.trim(),
                options = if (mode == QuizMode.CHOOSE) {
                    (distractors + entry.term.trim()).shuffled(random)
                } else {
                    emptyList()
                },
                mode = mode,
                languageCode = entry.languageCode
            )
        }
    }
}
