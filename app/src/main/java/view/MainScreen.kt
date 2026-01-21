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
import viewModel.CreatePostViewModel
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

    // ViewModel "persistenti" a livello MainScreen
    val feedViewModel: FeedViewModel = viewModel(factory = userFactory)
    val myUserProfileViewModel: MyUserProfileViewModel = viewModel(factory = userFactory)
    val createPostViewModel: CreatePostViewModel = viewModel(factory = userFactory)

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
                    createPostViewModel = createPostViewModel,
                    modifier = Modifier.padding(innerPadding),
                    onBackToFeed = { mainScreenViewModel.navigateTo(AppScreen.Feed) },
                    onPostCreated = {
                        myUserProfileViewModel.refresh()
                    }
                )
            }

            is AppScreen.MyProfile -> {
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
                    onBackClick = {
                        if (userProfileViewModel.followChanged) {
                            feedViewModel.refresh()
                            userProfileViewModel.resetFollowChanged()
                        }
                        mainScreenViewModel.navigateTo(AppScreen.Feed)
                    }
                )
            }

            is AppScreen.PostMap -> {
                // TODO: MapScreen
            }
        }
    }
}