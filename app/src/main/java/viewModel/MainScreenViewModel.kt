package viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import repository.ApiRepository
import repository.SettingsRepository

enum class MainTab {
    FEED,
    CREATEPOST,
    USERPROFILE
}
class MainScreenViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    // Tab corrente
    var currentTab = mutableStateOf(MainTab.FEED)
    private set


    fun changeTab(newTab: MainTab) {
        currentTab.value = newTab
    }

}
