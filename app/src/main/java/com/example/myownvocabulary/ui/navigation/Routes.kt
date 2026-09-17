package com.example.myownvocabulary.ui.navigation

object Routes {
    const val WORDS = "words"
    const val ADD_WORD = "add_word"
    const val LEARN = "learn"
    const val SETTINGS = "settings"

    const val WORD_DETAIL = "word/{wordId}"
    fun wordDetail(wordId: String) = "word/$wordId"

    val Tabs = setOf(WORDS, LEARN, SETTINGS)
}
