package com.akshar.dyslexiareader.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.akshar.dyslexiareader.domain.model.UserSettings
import com.akshar.dyslexiareader.util.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "akshar_user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val PARENTAL_CONSENT_GIVEN = booleanPreferencesKey("parental_consent_given")
        val ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
        val PARENT_ACCOUNT_ID = stringPreferencesKey("parent_account_id")
        val IS_OFFLINE_MODE = booleanPreferencesKey("is_offline_mode")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val RULER_ENABLED = booleanPreferencesKey("ruler_enabled")
        val BIONIC_ENABLED = booleanPreferencesKey("bionic_enabled")
        val SYLLABLES_ENABLED = booleanPreferencesKey("syllables_enabled")
        val READER_THEME = stringPreferencesKey("reader_theme")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val LETTER_SPACING = floatPreferencesKey("letter_spacing")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val TTS_SPEED = floatPreferencesKey("tts_speed")
        val TTS_PITCH = floatPreferencesKey("tts_pitch")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            parentalConsentGiven = prefs[PreferencesKeys.PARENTAL_CONSENT_GIVEN] ?: false,
            activeProfileId = prefs[PreferencesKeys.ACTIVE_PROFILE_ID],
            parentAccountId = prefs[PreferencesKeys.PARENT_ACCOUNT_ID],
            isOfflineMode = prefs[PreferencesKeys.IS_OFFLINE_MODE] ?: true,
            appLanguage = AppLanguage.fromCode(prefs[PreferencesKeys.APP_LANGUAGE]),
            rulerEnabled = prefs[PreferencesKeys.RULER_ENABLED] ?: false,
            bionicEnabled = prefs[PreferencesKeys.BIONIC_ENABLED] ?: false,
            syllablesEnabled = prefs[PreferencesKeys.SYLLABLES_ENABLED] ?: false,
            readerTheme = prefs[PreferencesKeys.READER_THEME] ?: "cream",
            fontSize = prefs[PreferencesKeys.FONT_SIZE] ?: 22f,
            lineSpacing = prefs[PreferencesKeys.LINE_SPACING] ?: 1.6f,
            letterSpacing = prefs[PreferencesKeys.LETTER_SPACING] ?: 0.08f,
            fontFamily = prefs[PreferencesKeys.FONT_FAMILY] ?: "lexend",
            ttsSpeed = prefs[PreferencesKeys.TTS_SPEED] ?: 1.0f,
            ttsPitch = prefs[PreferencesKeys.TTS_PITCH] ?: 1.0f
        )
    }

    suspend fun setParentalConsent(given: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PARENTAL_CONSENT_GIVEN] = given
        }
    }

    suspend fun setActiveProfileId(profileId: String?) {
        context.dataStore.edit { prefs ->
            if (profileId != null) {
                prefs[PreferencesKeys.ACTIVE_PROFILE_ID] = profileId
            } else {
                prefs.remove(PreferencesKeys.ACTIVE_PROFILE_ID)
            }
        }
    }

    suspend fun setParentAccount(accountId: String?, isOffline: Boolean) {
        context.dataStore.edit { prefs ->
            if (accountId != null) {
                prefs[PreferencesKeys.PARENT_ACCOUNT_ID] = accountId
            } else {
                prefs.remove(PreferencesKeys.PARENT_ACCOUNT_ID)
            }
            prefs[PreferencesKeys.IS_OFFLINE_MODE] = isOffline
        }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.APP_LANGUAGE] = language.code
        }
    }

    suspend fun updateReaderPreferences(
        theme: String? = null,
        fontSize: Float? = null,
        lineSpacing: Float? = null,
        letterSpacing: Float? = null,
        fontFamily: String? = null,
        rulerEnabled: Boolean? = null,
        bionicEnabled: Boolean? = null,
        syllablesEnabled: Boolean? = null,
        ttsSpeed: Float? = null,
        ttsPitch: Float? = null
    ) {
        context.dataStore.edit { prefs ->
            theme?.let { prefs[PreferencesKeys.READER_THEME] = it }
            fontSize?.let { prefs[PreferencesKeys.FONT_SIZE] = it }
            lineSpacing?.let { prefs[PreferencesKeys.LINE_SPACING] = it }
            letterSpacing?.let { prefs[PreferencesKeys.LETTER_SPACING] = it }
            fontFamily?.let { prefs[PreferencesKeys.FONT_FAMILY] = it }
            rulerEnabled?.let { prefs[PreferencesKeys.RULER_ENABLED] = it }
            bionicEnabled?.let { prefs[PreferencesKeys.BIONIC_ENABLED] = it }
            syllablesEnabled?.let { prefs[PreferencesKeys.SYLLABLES_ENABLED] = it }
            ttsSpeed?.let { prefs[PreferencesKeys.TTS_SPEED] = it }
            ttsPitch?.let { prefs[PreferencesKeys.TTS_PITCH] = it }
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
