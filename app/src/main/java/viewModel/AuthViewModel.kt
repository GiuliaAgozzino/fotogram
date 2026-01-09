package viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import repository.SettingsRepository

class AuthViewModel (private val settingsRepository: SettingsRepository  // ✅ Usa SettingsRepository
) : ViewModel() {


    private fun setUserIdSessionId(userId: Int, sessionId: String){
        viewModelScope.launch {
            settingsRepository.saveUserIsSessionId(userId, sessionId)
        }
    }

    fun isUserNameOk(userName: String): Boolean {
        return userName.length <= 15
    }

}