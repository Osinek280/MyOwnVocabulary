package com.example.myownvocabulary.domain.quiz

object QuizAnswerMatcher {
    private val punctuation = Regex("\\p{P}+")
    private val whitespace = Regex("\\s+")

    fun matches(answer: String, correctAnswer: String): Boolean {
        val normalizedAnswer = normalize(answer)
        return normalizedAnswer.isNotEmpty() && normalizedAnswer == normalize(correctAnswer)
    }

    private fun normalize(value: String): String = value
        .lowercase()
        .replace(punctuation, "")
        .replace(whitespace, " ")
        .trim()
}
