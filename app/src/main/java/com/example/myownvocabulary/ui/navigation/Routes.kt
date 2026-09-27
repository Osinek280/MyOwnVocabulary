package com.example.myownvocabulary.ui.navigation

object Routes {
    const val ENTRIES = "entries"
    const val ADD_ENTRY = "add_entry"
    const val LEARN = "learn"
    const val SETTINGS = "settings"

    const val ENTRY_DETAIL = "entry/{entryId}"
    fun entryDetail(entryId: String) = "entry/$entryId"

    val Tabs = setOf(ENTRIES, LEARN, SETTINGS)
}
