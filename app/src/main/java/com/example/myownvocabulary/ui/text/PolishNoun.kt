package com.example.myownvocabulary.ui.text

enum class PolishNumeralCase {
    NominativeSingular,
    NominativePlural,
    GenitivePlural;

    companion object {
        fun forCount(count: Int): PolishNumeralCase {
            val absoluteCount = kotlin.math.abs(count)
            val lastDigit = absoluteCount % 10
            val lastTwoDigits = absoluteCount % 100
            return when {
                absoluteCount == 1 -> NominativeSingular
                lastDigit in 2..4 && lastTwoDigits !in 12..14 -> NominativePlural
                else -> GenitivePlural
            }
        }
    }
}

data class PolishNoun(
    val nominativeSingular: String,
    val nominativePlural: String,
    val genitivePlural: String,
) {
    fun inflect(count: Int): String = when (PolishNumeralCase.forCount(count)) {
        PolishNumeralCase.NominativeSingular -> nominativeSingular
        PolishNumeralCase.NominativePlural -> nominativePlural
        PolishNumeralCase.GenitivePlural -> genitivePlural
    }

    fun withCount(count: Int): String = "$count ${inflect(count)}"
}

val ExpressionNoun = PolishNoun(
    nominativeSingular = "wyrażenie",
    nominativePlural = "wyrażenia",
    genitivePlural = "wyrażeń",
)
