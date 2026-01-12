package view

import android.content.Context
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fotogram.ui.theme.FotogramTheme
import repository.ApiRepository
import repository.SettingsRepository
import viewModel.AppViewModel
import viewModel.AuthViewModel
import viewModel.AuthViewModelFactory
import viewModel.UserViewModelFactory

private val Context.userDataStore by preferencesDataStore(name = "user_prefs")

class MainActivity : ComponentActivity() {

    private lateinit var apiRepository: ApiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepository = SettingsRepository(applicationContext.userDataStore)
        apiRepository = ApiRepository()

        val authFactory = AuthViewModelFactory(settingsRepository, apiRepository)

        enableEdgeToEdge()

        setContent {
            FotogramTheme {
                val appViewModel: AppViewModel = viewModel(factory = authFactory)

                App(
                    appViewModel = appViewModel,
                    authFactory = authFactory,
                    apiRepository = apiRepository
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::apiRepository.isInitialized) {
            apiRepository.close()
        }
    }
}

@Composable
fun App(
    appViewModel: AppViewModel,
    authFactory: AuthViewModelFactory,
    apiRepository: ApiRepository
) {
    when (appViewModel.isLoggedIn) {
        null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        false -> {
            val authViewModel: AuthViewModel = viewModel(factory = authFactory)

            SignInScreen(
                viewModel = authViewModel,
                onRegistrationSuccess = {
                    appViewModel.setLoggedIn(true)
                }
            )
        }

        true -> {
            // Crea UserViewModelFactory solo quando hai userId e sessionId
            val userFactory = UserViewModelFactory(
                userId = appViewModel.userId,
                sessionId = appViewModel.sessionId,
                apiRepository = apiRepository
            )

            MainScreen(
                userFactory = userFactory,
            )
        }
    }
}