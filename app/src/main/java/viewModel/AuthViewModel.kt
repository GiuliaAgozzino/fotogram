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

    var showError by mutableStateOf(false)
        private set

    fun register(userName: String, pictureBase64: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            showError = false

            try {
                val result = apiRepository.register(userName, pictureBase64)

                if (result.isSuccess) {
                    val response = result.getOrNull()!!

                    settingsRepository.saveUserIsSessionId(
                        userId = response.userId,
                        sessionId = response.sessionId
                    )

                    Log.d("AuthViewModel", "Registrazione completata")
                    onSuccess()
                } else {
                    showError = true
                    Log.e("AuthViewModel", "Errore registrazione", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                showError = true
                Log.e("AuthViewModel", "Eccezione", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        showError = false
    }
}