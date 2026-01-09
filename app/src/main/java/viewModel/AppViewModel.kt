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

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus(){
        viewModelScope.launch {
            isLoggedIn = settingsRepository.isLogged()
        }
    }
}