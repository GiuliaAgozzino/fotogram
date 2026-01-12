package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import repository.ApiRepository
import repository.SettingsRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val settingsRepository: SettingsRepository,
    private val apiRepository: ApiRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun register(userName: String, pictureBase64: String, onSuccess: () -> Unit) {
        if (!isUserNameOk(userName)) {
            errorMessage = "Nome utente non valido (max 15 caratteri)"
            return
        }

        if (pictureBase64.isEmpty()) {
            errorMessage = "Seleziona un'immagine di profilo"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val result = apiRepository.register(userName, pictureBase64)

                if (result.isSuccess) {
                    val response = result.getOrNull()!!

                    Log.d("Auth-AuthViewModel", "Registrazione ok, salvo dati...")

                    // Salva e aspetta che sia completato
                    settingsRepository.saveUserIsSessionId(
                        userId = response.userId,
                        sessionId = response.sessionId
                    )

                    // Verifica che sia salvato
                    val savedUserId = settingsRepository.getUserId()
                    val savedSessionId = settingsRepository.getSessionId()
                    Log.d("AuthViewModel", "Verifica salvataggio: userId=$savedUserId, sessionId=$savedSessionId")

                    onSuccess()

                } else {
                    val exception = result.exceptionOrNull()
                    errorMessage = "Errore: ${exception?.message}"
                    Log.e("AuthViewModel", "Errore registrazione", exception)
                }

            } catch (e: Exception) {
                errorMessage = "Errore di rete: ${e.message}"
                Log.e("AuthViewModel", "Eccezione", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun isUserNameOk(userName: String): Boolean {
        return userName.isNotEmpty() && userName.length <= 15
    }

    fun clearError() {
        errorMessage = null
    }
}
