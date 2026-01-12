package viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import repository.ApiRepository
import repository.SettingsRepository


class ViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val apiRepository: ApiRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AppViewModel::class.java) -> {
                AppViewModel(settingsRepository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(settingsRepository, apiRepository) as T
            }
            modelClass.isAssignableFrom(MainScreenViewModel::class.java) -> {
                MainScreenViewModel(settingsRepository) as T
            }
            modelClass.isAssignableFrom(FeedViewModel::class.java) -> {
                FeedViewModel(settingsRepository, apiRepository) as T
            }
            else -> throw IllegalArgumentException("ViewModel sconosciuto: ${modelClass.name}")
        }
    }
}