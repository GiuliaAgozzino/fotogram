package viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import repository.SettingsRepository

class AppViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    var isLoggedIn by mutableStateOf<Boolean?>(null)
        private set

    var userId by mutableStateOf<Int?>(null)
        private set

    var sessionId by mutableStateOf<String?>(null)
        private set

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                val logged = settingsRepository.getUserId() != null
                if (logged) {
                    userId = settingsRepository.getUserId()
                    sessionId = settingsRepository.getSessionId()
                }
                isLoggedIn = logged
            } catch (e: Exception) {
                e.printStackTrace()
                isLoggedIn = false
            }
        }
    }

    fun setLoggedIn(value: Boolean) {
        if (value) {
            viewModelScope.launch {
                userId = settingsRepository.getUserId()
                sessionId = settingsRepository.getSessionId()
                isLoggedIn = true
            }
        } else {
            isLoggedIn = false
        }
    }
}