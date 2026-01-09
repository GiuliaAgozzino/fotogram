package repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private companion object{
        val USER_ID = intPreferencesKey("user_id")
        val SESSION_ID = stringPreferencesKey("session_id")
    }

    suspend fun  getSessionId(): String?{
        val prefs = dataStore.data.first()
        return prefs[SESSION_ID]
    }

    suspend fun  getUserId(): Int?{
        val  prefs = dataStore.data.first()
        return  prefs[USER_ID]
    }

    suspend fun saveUserIsSessionId( userId: Int, sessionId: String){
        dataStore.edit { prefs ->
            prefs[USER_ID] = userId
            prefs[SESSION_ID] = sessionId
        }
    }

    suspend fun isLogged(): Boolean{
        return getUserId() != null
    }

}
