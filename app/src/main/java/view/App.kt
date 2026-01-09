package view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.preferencesDataStore
import com.example.fotogram.ui.theme.FotogramTheme
import repository.SettingsRepository
import viewModel.AppViewModel

private val android.content.Context.userDataStore by preferencesDataStore(name = "user_prefs")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val userDataStore = applicationContext.userDataStore

        val settingsRepository = SettingsRepository(userDataStore)
        val appViewModel = AppViewModel(settingsRepository)
        val authViewModel = AuthViewModel(settingsRepository)

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


}