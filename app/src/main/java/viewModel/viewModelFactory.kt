// AuthViewModelFactory.kt
package viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import repository.ApiRepository
import repository.SettingsRepository

class AuthViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val apiRepository: ApiRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AppViewModel::class.java) -> {
                AppViewModel(settingsRepository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(settingsRepository, apiRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

class MyUserViewModelFactory(
    private val userId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(FeedViewModel::class.java) -> {
                FeedViewModel(userId, sessionId, apiRepository) as T
            }
             modelClass.isAssignableFrom(CreatePostViewModel::class.java) -> {
             CreatePostViewModel(userId, sessionId, apiRepository) as T
            }
            modelClass.isAssignableFrom(MyUserProfileViewModel::class.java) -> {
                MyUserProfileViewModel(userId, sessionId, apiRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

class UserProfileViewModelFactory(
    private val targetUserId: Int,      // L'utente da visualizzare
    private val sessionId: String?,     // Sessione dell'utente loggato
    private val apiRepository: ApiRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(UserProfileViewModel::class.java) -> {
                UserProfileViewModel(targetUserId, sessionId, apiRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}