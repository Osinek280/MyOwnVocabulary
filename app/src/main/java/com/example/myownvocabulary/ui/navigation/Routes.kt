package com.example.myownvocabulary.ui.navigation

import com.example.myownvocabulary.data.entry.EntryKind

object Routes {
    const val ENTRIES = "entries"
    const val LEARN = "learn"
    const val SETTINGS = "settings"

    const val ENTRY_DETAIL = "entry/{entryId}"
    fun entryDetail(entryId: String) = "entry/$entryId"

    const val ADD_ENTRY = "add_entry/{kind}"
    fun addEntry(kind: EntryKind) = "add_entry/${kind.name}"

    val Tabs = setOf(ENTRIES, LEARN, SETTINGS)
}
