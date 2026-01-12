package view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.fotogram.ui.theme.FotogramTheme
import repository.ApiRepository
import repository.SettingsRepository
import viewModel.AppViewModel
import viewModel.AuthViewModel
import viewModel.FeedViewModel
import viewModel.MainScreenViewModel
import viewModel.ViewModelFactory



private val android.content.Context.userDataStore by preferencesDataStore(name = "user_prefs")

class MainActivity : ComponentActivity() {

    private lateinit var appViewModel: AppViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var mainScreenViewModel: MainScreenViewModel
    private lateinit var feedViewModel: FeedViewModel

    private lateinit var apiRepository: ApiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepository = SettingsRepository(applicationContext.userDataStore)
        apiRepository = ApiRepository()

        val factory = ViewModelFactory(settingsRepository, apiRepository)

        appViewModel = ViewModelProvider(this, factory)[AppViewModel::class.java]
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        mainScreenViewModel = ViewModelProvider(this, factory)[MainScreenViewModel::class.java]
        feedViewModel = ViewModelProvider(this, factory)[FeedViewModel::class.java]

        enableEdgeToEdge()

        setContent {
            FotogramTheme {
                App(
                    appViewModel = appViewModel,
                    authViewModel = authViewModel,
                    mainScreenViewModel = mainScreenViewModel,
                    feedViewModel = feedViewModel
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
    authViewModel: AuthViewModel,
    mainScreenViewModel: MainScreenViewModel,
    feedViewModel: FeedViewModel
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
            SignInScreen(
                viewModel = authViewModel,
                onRegistrationSuccess = {
                    appViewModel.setLoggedIn(true)
                }
            )
        }
        true -> {
            MainScreen(
                viewModel = mainScreenViewModel,
                feedViewModel = feedViewModel,
                userId = appViewModel.userId,
                sessionId = appViewModel.sessionId
            )
        }
    }
}