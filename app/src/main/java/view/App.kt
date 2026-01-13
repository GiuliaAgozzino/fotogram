package view

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fotogram.ui.theme.FotogramTheme
import repository.ApiRepository
import repository.SettingsRepository
import viewModel.AppViewModel
import viewModel.AuthViewModel
import viewModel.AuthViewModelFactory
import viewModel.UserViewModelFactory
import view.common.LoadingIndicator

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
        null -> LoadingIndicator()

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