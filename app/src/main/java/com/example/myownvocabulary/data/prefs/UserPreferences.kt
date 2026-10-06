package com.example.myownvocabulary.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myownvocabulary.data.entry.Language
import com.example.myownvocabulary.ui.components.quiz.QuizOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val MAX_RECENT_LANGUAGES = 5

val Context.userPrefs: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

private object UserPrefsKeys {
    val RecentLanguageCodes = stringPreferencesKey("recent_language_codes")
    val QuizShuffle = booleanPreferencesKey("quiz_shuffle")
    val QuizAnswerWithTerm = booleanPreferencesKey("quiz_answer_with_term")
    val QuizAnswerWithDefinition = booleanPreferencesKey("quiz_answer_with_definition")
    val QuizMultipleChoice = booleanPreferencesKey("quiz_multiple_choice")
    val QuizWritten = booleanPreferencesKey("quiz_written")
    val QuizRetypeCorrectAnswer = booleanPreferencesKey("quiz_retype_correct_answer")
}

class UserPreferences(private val context: Context) {
    val quizOptions: Flow<QuizOptions> = context.userPrefs.data.map { prefs ->
        val defaults = QuizOptions()
        QuizOptions(
            shuffle = prefs[UserPrefsKeys.QuizShuffle] ?: defaults.shuffle,
            answerWithTerm = prefs[UserPrefsKeys.QuizAnswerWithTerm] ?: defaults.answerWithTerm,
            answerWithDefinition = prefs[UserPrefsKeys.QuizAnswerWithDefinition] ?: defaults.answerWithDefinition,
            multipleChoice = prefs[UserPrefsKeys.QuizMultipleChoice] ?: defaults.multipleChoice,
            written = prefs[UserPrefsKeys.QuizWritten] ?: defaults.written,
            retypeCorrectAnswer = prefs[UserPrefsKeys.QuizRetypeCorrectAnswer] ?: defaults.retypeCorrectAnswer
        )
    }

    suspend fun saveQuizOptions(options: QuizOptions) {
        context.userPrefs.edit { prefs ->
            prefs[UserPrefsKeys.QuizShuffle] = options.shuffle
            prefs[UserPrefsKeys.QuizAnswerWithTerm] = options.answerWithTerm
            prefs[UserPrefsKeys.QuizAnswerWithDefinition] = options.answerWithDefinition
            prefs[UserPrefsKeys.QuizMultipleChoice] = options.multipleChoice
            prefs[UserPrefsKeys.QuizWritten] = options.written
            prefs[UserPrefsKeys.QuizRetypeCorrectAnswer] = options.retypeCorrectAnswer
        }
    }

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
                .take(MAX_RECENT_LANGUAGES)
            prefs[UserPrefsKeys.RecentLanguageCodes] = updated.joinToString(",")
        }
    }
}

private fun parseLanguageCodes(raw: String?): List<String> = raw.orEmpty()
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
