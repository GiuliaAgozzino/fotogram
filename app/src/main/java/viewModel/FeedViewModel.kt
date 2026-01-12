package viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import repository.ApiRepository
import repository.SettingsRepository

class FeedViewModel(
    private val settingsRepository: SettingsRepository,
    private val apiRepository: ApiRepository
) : ViewModel() {

    var sessionId = mutableStateOf<String?>(null)
        private set
    var userId = mutableStateOf<Int?>(null)
        private set

    init {
        loadUserData()
    }

    // Rendi pubblica così può essere richiamata
    fun loadUserData() {
        Log.d("FeedViewModel", "loadUserData() chiamato")
        viewModelScope.launch {
            sessionId.value = settingsRepository.getSessionId()
            userId.value = settingsRepository.getUserId()
            Log.d("FeedViewModel", "Dati caricati: userId=${userId.value}, sessionId=${sessionId.value}")
        }
    }
}