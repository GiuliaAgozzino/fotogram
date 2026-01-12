package view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import viewModel.FeedViewModel
import viewModel.MainScreenViewModel
import viewModel.MainTab
import viewModel.UserProfileViewModel
import viewModel.UserViewModelFactory

@Composable
fun MainScreen(
    userFactory: UserViewModelFactory
) {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val currentTab by mainScreenViewModel.currentTab
    val feedViewModel: FeedViewModel = viewModel(factory = userFactory)
    val userProfileViewModel: UserProfileViewModel = viewModel (factory = userFactory)
    Scaffold(
        bottomBar = {
            if (currentTab != MainTab.CREATEPOST) {
                NavBar(
                    currentTab = mainScreenViewModel.currentTab.value,
                    onNavigate = { tab -> mainScreenViewModel.changeTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            MainTab.FEED -> FeedScreen(
                modifier = Modifier.padding(innerPadding),
                feedViewModel= feedViewModel
            )
            MainTab.CREATEPOST -> CreatePostScreen(
                modifier = Modifier.padding(innerPadding),
                onBackToFeed = { mainScreenViewModel.changeTab(MainTab.FEED) },

            )
            MainTab.USERPROFILE -> UserProfileScreen(
                modifier = Modifier.padding(innerPadding),
                userProfileViewModel = userProfileViewModel
            )
        }
    }
}