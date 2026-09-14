package com.example.myownvocabulary.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myownvocabulary.data.word.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val MaxRecentLanguages = 5

val Context.userPrefs: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

private object UserPrefsKeys {
    val RecentLanguageCodes = stringPreferencesKey("recent_language_codes")
}

class UserPreferences(private val context: Context) {
    val recentLanguages: Flow<List<Language>> = context.userPrefs.data.map { prefs ->
        parseLanguageCodes(prefs[UserPrefsKeys.RecentLanguageCodes])
            .mapNotNull { code ->
                Language.entries.find { it.code.equals(code, ignoreCase = true) }
            }
            .distinct()
    }

    suspend fun clearRecentLanguages() {
        context.userPrefs.edit { prefs ->
            prefs.remove(UserPrefsKeys.RecentLanguageCodes)
        }
    }

    suspend fun rememberLanguage(language: Language) {
        context.userPrefs.edit { prefs ->
            val current = parseLanguageCodes(prefs[UserPrefsKeys.RecentLanguageCodes])
            val updated = (listOf(language.code) + current.filter { it != language.code })
                .take(MaxRecentLanguages)
            prefs[UserPrefsKeys.RecentLanguageCodes] = updated.joinToString(",")
        }
    }
}

private fun parseLanguageCodes(raw: String?): List<String> =
    raw.orEmpty()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }