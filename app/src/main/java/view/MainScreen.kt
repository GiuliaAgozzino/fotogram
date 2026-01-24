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
import viewModel.*

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

    // DataViewModel condiviso (Single Source of Truth per Post e User)
    val dataViewModel: DataViewModel = viewModel(
        factory = DataViewModelFactory(apiRepository, sessionId)
    )

    // Factory per i ViewModel che necessitano di sessionId
    val feedViewModelFactory = remember(sessionId) {
        FeedViewModelFactory(sessionId, apiRepository)
    }

    // ViewModel persistenti a livello MainScreen
    val feedViewModel: FeedViewModel = viewModel(factory = feedViewModelFactory)
    val createPostViewModel: CreatePostViewModel = viewModel(
        factory = CreatePostViewModelFactory(sessionId, apiRepository)
    )
    val myUserProfileViewModel: MyUserProfileViewModel = viewModel(
        factory = MyUserProfileViewModelFactory(currentUserId, sessionId, apiRepository)
    )

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
                    dataViewModel = dataViewModel,
                    currentUserId = currentUserId,
                    onNavigateToProfile = { userId ->
                        if (userId == currentUserId) {
                            mainScreenViewModel.navigateTo(AppScreen.MyProfile)
                        } else {
                            mainScreenViewModel.navigateTo(AppScreen.UserProfile(userId))
                        }
                    }
                )
            }

            is AppScreen.CreatePost -> {
                CreatePostScreen(
                    createPostViewModel = createPostViewModel,
                    dataViewModel = dataViewModel,
                    modifier = Modifier.padding(innerPadding),
                    onBackToFeed = { mainScreenViewModel.navigateTo(AppScreen.Feed) },
                    onPostCreated = {
                        myUserProfileViewModel.refresh()
                        feedViewModel.refresh()
                    }
                )
            }

            is AppScreen.MyProfile -> {
                MyUserProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    userProfileViewModel = myUserProfileViewModel,
                    dataViewModel = dataViewModel
                )
            }

            is AppScreen.UserProfile -> {
                val userProfileViewModel: UserProfileViewModel = viewModel(
                    key = "user_${screen.userId}",
                    factory = UserProfileViewModelFactory(screen.userId, sessionId, apiRepository)
                )

                UserProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    userProfileViewModel = userProfileViewModel,
                    dataViewModel = dataViewModel,
                    onBackClick = {
                        if (userProfileViewModel.followChanged) {
                            feedViewModel.refresh()
                            userProfileViewModel.resetFollowChanged()
                        }
                        mainScreenViewModel.navigateTo(AppScreen.Feed)
                    }
                )
            }
        }
    }
}
