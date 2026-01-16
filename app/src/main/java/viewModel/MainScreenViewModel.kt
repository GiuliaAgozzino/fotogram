package viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

// Sealed class per gestire sia i tab che la navigazione a schermate esterne
sealed class AppScreen {
    // Tab principali (con bottom bar)
    object Feed : AppScreen()
    object CreatePost : AppScreen()
    object MyProfile : AppScreen()

    // Schermate secondarie (senza bottom bar, con back)
    data class UserProfile(val userId: Int) : AppScreen()
    data class PostMap(val postId: Int) : AppScreen()
}

class MainScreenViewModel : ViewModel() {

    // Schermata corrente
    var currentScreen = mutableStateOf<AppScreen>(AppScreen.Feed)
        private set

    // Stack per gestire il "back" (opzionale ma utile)
    private val backStack = mutableListOf<AppScreen>()

    fun navigateTo(screen: AppScreen) {
        // Salva la schermata corrente nello stack (se non è la stessa)
        if (currentScreen.value != screen) {
            backStack.add(currentScreen.value)
        }
        currentScreen.value = screen
    }

    // Per i tab: naviga senza aggiungere allo stack
    fun changeTab(screen: AppScreen) {
        // Pulisce lo stack quando cambi tab (comportamento tipico)
        backStack.clear()
        currentScreen.value = screen
    }

    fun goBack(): Boolean {
        return if (backStack.isNotEmpty()) {
            currentScreen.value = backStack.removeLast()
            true
        } else {
            false
        }
    }

    // Helper per sapere se mostrare la bottom bar
    fun shouldShowBottomBar(): Boolean {
        return when (currentScreen.value) {
            is AppScreen.Feed,
            is AppScreen.MyProfile -> true
            else -> false
        }
    }

    // Helper per sapere se siamo in un tab principale
    fun isMainTab(): Boolean = currentScreen.value in listOf(
        AppScreen.Feed,
        AppScreen.CreatePost,
        AppScreen.MyProfile
    )
}