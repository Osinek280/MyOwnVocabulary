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

    const val TRANSFER = "transfer/{mode}"
    const val TRANSFER_EXPORT = "export"
    const val TRANSFER_IMPORT = "import"
    fun transfer(mode: String) = "transfer/$mode"

    val Tabs = setOf(ENTRIES, LEARN, SETTINGS)
}
