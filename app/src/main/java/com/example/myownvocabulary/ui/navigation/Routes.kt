package com.example.myownvocabulary.ui.navigation

object Routes {
    const val Words = "words"
    const val AddWord = "add_word"
    const val Learn = "learn"
    const val Settings = "settings"

    const val WordDetail = "word/{wordId}"
    fun wordDetail(wordId: String) = "word/$wordId"

    val Tabs = setOf(Words, Learn, Settings)
}