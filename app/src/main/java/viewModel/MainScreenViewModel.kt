package viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

sealed class AppScreen {
    object Feed : AppScreen()
    object CreatePost : AppScreen()
    object MyProfile : AppScreen()
    data class UserProfile(val userId: Int) : AppScreen()
    data class PostMap(val postId: Int) : AppScreen()
}

class MainScreenViewModel : ViewModel() {

    var currentScreen = mutableStateOf<AppScreen>(AppScreen.Feed)
        private set

    fun navigateTo(screen: AppScreen) {
        currentScreen.value = screen
    }

    fun shouldShowBottomBar(): Boolean {
        return currentScreen.value in listOf(
            AppScreen.Feed,
            AppScreen.MyProfile
        )
    }
}
