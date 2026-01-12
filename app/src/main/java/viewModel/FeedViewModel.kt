package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import repository.ApiRepository
import repository.SettingsRepository



class FeedViewModel(
    val userId: Int?,
    val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {


    var userIdState by mutableStateOf(userId)
        private set

    var sessionIdState by mutableStateOf(sessionId)
        private set


}
