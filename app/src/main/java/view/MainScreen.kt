package view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import repository.ApiRepository
import viewModel.AppScreen
import viewModel.FeedViewModel
import viewModel.MainScreenViewModel
import viewModel.MyUserProfileViewModel
import viewModel.MyUserViewModelFactory
import viewModel.UserProfileViewModel
import viewModel.UserProfileViewModelFactory

@Composable
fun MainScreen(
    currentUserId: Int,
    sessionId: String,
    apiRepository: ApiRepository
) {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val currentScreen by mainScreenViewModel.currentScreen

    // Factory per i ViewModel dell'utente loggato
    val userFactory = remember(currentUserId, sessionId) {
        MyUserViewModelFactory(currentUserId, sessionId, apiRepository)
    }

    // Gestione del tasto back di sistema
    BackHandler(enabled = !mainScreenViewModel.isMainTab()) {
        mainScreenViewModel.goBack()
    }

    Scaffold(
        bottomBar = {
            if (mainScreenViewModel.shouldShowBottomBar()) {
                NavBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> mainScreenViewModel.changeTab(screen) }
                )
            }
        }
    ) { innerPadding ->

        when (val screen = currentScreen) {

            // ===== TAB PRINCIPALI =====

            is AppScreen.Feed -> {
                val feedViewModel: FeedViewModel = viewModel(factory = userFactory)
                FeedScreen(
                    modifier = Modifier.padding(innerPadding),
                    feedViewModel = feedViewModel,
                    currentUserId = currentUserId,
                    onNavigateToProfile = { userId ->
                        if (userId == currentUserId) {
                            mainScreenViewModel.changeTab(AppScreen.MyProfile)
                        } else {
                            mainScreenViewModel.navigateTo(AppScreen.UserProfile(userId))
                        }
                    },
                    onNavigateToMap = { postId ->
                        mainScreenViewModel.navigateTo(AppScreen.PostMap(postId))
                    }
                )
            }

            is AppScreen.CreatePost -> {
                CreatePostScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBackToFeed = { mainScreenViewModel.changeTab(AppScreen.Feed) }
                )
            }

            is AppScreen.MyProfile -> {
                val myUserProfileViewModel: MyUserProfileViewModel = viewModel(factory = userFactory)
                MyUserProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    userProfileViewModel = myUserProfileViewModel
                )
            }

            // ===== SCHERMATE SECONDARIE =====

            is AppScreen.UserProfile -> {
                // Crea factory specifica per questo utente
                val userProfileFactory = remember(screen.userId) {
                    UserProfileViewModelFactory(screen.userId, sessionId, apiRepository)
                }
                val userProfileViewModel: UserProfileViewModel = viewModel(
                    key = "user_${screen.userId}",
                    factory = userProfileFactory
                )

                UserProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    userProfileViewModel = userProfileViewModel,
                    onBackClick = { mainScreenViewModel.goBack() },
                    onNavigateToUserProfile = { userId ->
                        if (userId == currentUserId) {
                            // Torna al tab del mio profilo
                            mainScreenViewModel.changeTab(AppScreen.MyProfile)
                        } else {
                            mainScreenViewModel.navigateTo(AppScreen.UserProfile(userId))
                        }
                    }
                )
            }

            is AppScreen.PostMap -> {
                // TODO: Implementare MapScreen
                // MapScreen(
                //     postId = screen.postId,
                //     onBackClick = { mainScreenViewModel.goBack() }
                // )
            }
        }
    }
}