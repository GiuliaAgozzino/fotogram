package viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point

sealed class AppScreen {
    object Feed : AppScreen()
    object CreatePost : AppScreen()
    object MyProfile : AppScreen()
    data class UserProfile(val userId: Int) : AppScreen()

    //Screen per aprire una mappa, in una nuova schemarta, a cui passi un point generico
    data class Location(
        val point: Point,
        val label: String = "Posizione",
        val markerLabel: String? = null,
        val maxDistanceKm: Double = 5.0,
        val showDistanceInfo: Boolean = true
    ) : AppScreen()
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
