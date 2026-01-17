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


    BackHandler(enabled = currentScreen !is AppScreen.Feed) {
        mainScreenViewModel.navigateTo(AppScreen.Feed)
    }
    val userFactory = remember(currentUserId, sessionId) {
        MyUserViewModelFactory(currentUserId, sessionId, apiRepository)
    }

    Scaffold(
        bottomBar = {
            if (mainScreenViewModel.shouldShowBottomBar()) {
                NavBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> mainScreenViewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->

        when (val screen = currentScreen) {

            is AppScreen.Feed -> {
                val feedViewModel: FeedViewModel = viewModel(factory = userFactory)
                FeedScreen(
                    modifier = Modifier.padding(innerPadding),
                    feedViewModel = feedViewModel,
                    currentUserId = currentUserId,
                    onNavigateToProfile = { userId ->
                        if (userId == currentUserId) {
                            mainScreenViewModel.navigateTo(AppScreen.MyProfile)
                        } else {
                            mainScreenViewModel.navigateTo(AppScreen.UserProfile(userId))
                        }
                    },
                    onNavigateToMap = { }
                )
            }

            is AppScreen.CreatePost -> {
                CreatePostScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBackToFeed = { mainScreenViewModel.navigateTo(AppScreen.Feed) }
                )
            }

            is AppScreen.MyProfile -> {
                val myUserProfileViewModel: MyUserProfileViewModel = viewModel(factory = userFactory)
                MyUserProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    userProfileViewModel = myUserProfileViewModel
                )
            }

            is AppScreen.UserProfile -> {
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
                    onBackClick = { mainScreenViewModel.navigateTo(AppScreen.Feed) }
                )
            }

            is AppScreen.PostMap -> {
                // TODO: MapScreen
            }
        }
    }
}