package view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.preferencesDataStore
import com.example.fotogram.ui.theme.FotogramTheme
import repository.SettingsRepository
import viewModel.AppViewModel
import viewModel.AuthViewModel
import repository.ApiRepository


private val android.content.Context.userDataStore by preferencesDataStore(name = "user_prefs")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userDataStore = applicationContext.userDataStore

        // Crea i repository
        val settingsRepository = SettingsRepository(userDataStore)
        val apiRepository = ApiRepository()

        // Crea i ViewModel passando i repository
        val appViewModel = AppViewModel(settingsRepository)
        val authViewModel = AuthViewModel(settingsRepository, apiRepository)

        enableEdgeToEdge()

        setContent {
            FotogramTheme {
                App(
                    appViewModel = appViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

@Composable
fun App(
    appViewModel: AppViewModel,
    authViewModel: AuthViewModel
) {
    when (appViewModel.isLoggedIn) {
        null -> {
            // Stato di caricamento
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        false -> {
            // Non loggato - mostra schermata di registrazione
            SignInScreen(
                viewModel = authViewModel,
                onRegistrationSuccess = {
                    appViewModel.setLoggedIn(true)
                }
            )
        }
        true -> {
            // Loggato - TODO: mostra home/bacheca
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text("Home - TODO")

            }
        }
    }
}