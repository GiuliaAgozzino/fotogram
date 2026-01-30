package viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import repository.ApiRepository
import repository.SettingsRepository

// ==================== AUTH ====================

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



// ==================== FEED ====================

class FeedViewModelFactory(
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            return FeedViewModel(sessionId, apiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// ==================== CREATE POST ====================

class CreatePostViewModelFactory(
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
            return CreatePostViewModel(sessionId, apiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// ==================== MY USER PROFILE ====================

class MyUserProfileViewModelFactory(
    private val userId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyUserProfileViewModel::class.java)) {
            return MyUserProfileViewModel(userId, sessionId, apiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// ==================== USER PROFILE ====================

class UserProfileViewModelFactory(
    private val targetUserId: Int,
    private val sessionId: String?,
    private val currentUserId: Int,
    private val apiRepository: ApiRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            return UserProfileViewModel(targetUserId, sessionId, currentUserId, apiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
