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

    // Inizia come null per indicare che stiamo ancora caricando
    var isLoggedIn by mutableStateOf<Boolean?>(null)
        private set

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                isLoggedIn = settingsRepository.isLogged()
            } catch (e: Exception) {
                e.printStackTrace()
                isLoggedIn = false // In caso di errore, considera non loggato
            }
        }
    }

    fun setLoggedIn(value: Boolean) {
        isLoggedIn = value
    }
}