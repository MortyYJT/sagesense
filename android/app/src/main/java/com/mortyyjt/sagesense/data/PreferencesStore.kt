package com.mortyyjt.sagesense.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sagesense_preferences")

class PreferencesStore(private val context: Context) {
    private val languageKey = stringPreferencesKey("language")
    private val onboardingKey = booleanPreferencesKey("onboarding_complete")

    val language: Flow<String> = context.dataStore.data.map { it[languageKey] ?: "en-AU" }
    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { it[onboardingKey] ?: false }

    suspend fun setLanguage(value: String) {
        context.dataStore.edit { it[languageKey] = value }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { it[onboardingKey] = true }
    }
}
