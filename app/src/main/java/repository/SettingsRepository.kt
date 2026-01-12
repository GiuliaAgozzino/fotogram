package repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private companion object{
        val USER_ID = intPreferencesKey("user_id")
        val SESSION_ID = stringPreferencesKey("session_id")
    }

    suspend fun getSessionId(): String? {
        return try {
            dataStore.data.map { prefs ->
                prefs[SESSION_ID]
            }.first()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserId(): Int? {
        return try {
            dataStore.data.map { prefs ->
                prefs[USER_ID]
            }.first()
        } catch (e: Exception) {
            null
        }
    }

    // In SettingsRepository.kt
    suspend fun saveUserIsSessionId(userId: Int, sessionId: String) {
        Log.d("SettingsRepo", "saveUserIsSessionId chiamato con userId=$userId")
        try {
            dataStore.edit { prefs ->
                prefs[USER_ID] = userId
                prefs[SESSION_ID] = sessionId
            }
            Log.d("SettingsRepo", "Salvataggio completato!")
        } catch (e: Exception) {
            Log.e("SettingsRepo", "Errore salvataggio", e)
        }
    }

    suspend fun isLogged(): Boolean {
        return try {
            getUserId() != null
        } catch (e: Exception) {
            false
        }
    }
}