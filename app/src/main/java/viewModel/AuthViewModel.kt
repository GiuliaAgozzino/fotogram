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

    // Stato per la UI
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Registra un nuovo utente
     * Segue la slide 17 del prof (gestione funzione asincrona con viewModelScope.launch)
     */
    fun register(userName: String, pictureBase64: String, onSuccess: () -> Unit) {
        // Validazione input
        if (!isUserNameOk(userName)) {
            errorMessage = "Nome utente non valido (max 15 caratteri)"
            return
        }

        if (pictureBase64.isEmpty()) {
            errorMessage = "Seleziona un'immagine di profilo"
            return
        }

        // Chiamata asincrona (come slide 17 del prof)
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Chiamata al repository
                val result = apiRepository.register(userName, pictureBase64)

                result.onSuccess { response ->
                    // Salva i dati ricevuti (userId e sessionId)
                    settingsRepository.saveUserIsSessionId(
                        userId = response.userId,
                        sessionId = response.sessionId
                    )

                    Log.d("AuthViewModel", "Registrazione completata! UID: ${response.userId}")

                    // Notifica il successo alla UI
                    onSuccess()

                }.onFailure { exception ->
                    // Gestione errore (come slide 21 del prof)
                    errorMessage = "Errore durante la registrazione: ${exception.message}"
                    Log.e("AuthViewModel", "Errore registrazione", exception)
                }

            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Valida il nome utente (max 15 caratteri come da progetto)
     */
    fun isUserNameOk(userName: String): Boolean {
        return userName.isNotEmpty() && userName.length <= 15
    }

    /**
     * Pulisce il messaggio di errore
     */
    fun clearError() {
        errorMessage = null
    }
}