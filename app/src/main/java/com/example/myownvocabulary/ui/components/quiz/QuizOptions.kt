package com.example.myownvocabulary.ui.components.quiz

import androidx.compose.runtime.saveable.listSaver

/** Configuration exposed to the quiz engine when an option changes. */
data class QuizOptions(
    val shuffle: Boolean = false,
//    val speech: Boolean = false,
    val answerWithTerm: Boolean = true,
    val answerWithDefinition: Boolean = true,
//    val flashcards: Boolean = false,
    val multipleChoice: Boolean = false,
    val written: Boolean = true,
//    val grading: WrittenGrading = WrittenGrading.MODERATE,
    val retypeCorrectAnswer: Boolean = false
)

// enum class WrittenGrading(val label: String, val description: String) {
//    RELAXED(
//        "Luźne",
//        "Ogólne znaczenie odpowiedzi można uznać za poprawne. Dopuszczalne są również synonimy, parafrazy i literówki."
//    ),
//    MODERATE(
//        "Umiarkowane",
//        "Wymagane jest dokładne dopasowanie, lecz dopuszczalne są błędy ortograficzne " +
//            "(np. znaki diakrytyczne, brakujące litery)."
//    ),
//    STRICT(
//        "Rygorystyczne",
//        "Wymagane jest dokładne dopasowanie. Dopuszczalne są jedynie drobne błędy stylistyczne " +
//            "(np. wielkość liter, interpunkcja lub tekst w nawiasach)."
//    )
// }

internal val QuizOptionsSaver = listSaver<QuizOptions, Any>(
    save = {
        listOf(
            it.shuffle,
            it.answerWithTerm,
            it.answerWithDefinition,
            it.multipleChoice,
            it.written,
            it.retypeCorrectAnswer
        )
    },
    restore = {
        QuizOptions(
            it[0] as Boolean,
            it[1] as Boolean,
            it[2] as Boolean,
            it[3] as Boolean,
            it[4] as Boolean,
            it[5] as Boolean
        )
    }
)

// internal val QuizOptionsSaver = listSaver<QuizOptions, Any>(
//    save = {
//        listOf(
//            it.shuffle, it.speech, it.answerWithTerm, it.answerWithDefinition,
//            it.flashcards, it.multipleChoice, it.written, it.grading.name, it.retypeCorrectAnswer
//        )
//    },
//    restore = {
//        QuizOptions(
//            it[0] as Boolean, it[1] as Boolean, it[2] as Boolean, it[3] as Boolean,
//            it[4] as Boolean, it[5] as Boolean, it[6] as Boolean,
//            WrittenGrading.valueOf(it[7] as String), it[8] as Boolean
//        )
//    }
// )
