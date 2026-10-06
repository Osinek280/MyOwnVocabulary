package com.example.myownvocabulary.domain.quiz

enum class QuizMode { CHOOSE, TYPE }

data class QuizQuestion(
    val prompt: String,
    val correctAnswer: String,
    val options: List<String> = emptyList(),
    val mode: QuizMode = QuizMode.CHOOSE,
    val languageCode: String
)
