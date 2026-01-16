package view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import viewModel.FeedViewModel
import viewModel.MainScreenViewModel
import viewModel.MainTab
import viewModel.MyUserProfileViewModel
import viewModel.MyUserViewModelFactory
import viewModel.UserProfileViewModel

@Composable
fun MainScreen(
    userFactory: MyUserViewModelFactory
) {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val currentTab by mainScreenViewModel.currentTab


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
            MainTab.FEED ->{
                val feedViewModel: FeedViewModel = viewModel(factory = userFactory)
                FeedScreen(
                    modifier = Modifier.padding(innerPadding),
                    feedViewModel= feedViewModel
                )
            }
            MainTab.CREATEPOST -> CreatePostScreen(
                modifier = Modifier.padding(innerPadding),
                onBackToFeed = { mainScreenViewModel.changeTab(MainTab.FEED) },

            )
            MainTab.MYUSERPROFILE -> {
                val myuserProfileViewModel: MyUserProfileViewModel = viewModel (factory = userFactory)
                MyUserProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    userProfileViewModel = myuserProfileViewModel,
                )
            }


        }
    }
}