package view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import viewModel.FeedViewModel
import viewModel.MainScreenViewModel
import viewModel.MainTab

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    feedViewModel: FeedViewModel,
    userId: Int?,
    sessionId: String?
) {
    val currentTab by viewModel.currentTab

    Scaffold(
        bottomBar = {
            if (currentTab != MainTab.CREATEPOST) {
                NavBar(
                    currentTab = viewModel.currentTab.value,
                    onNavigate = { tab -> viewModel.changeTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            MainTab.FEED -> FeedScreen(
                modifier = Modifier.padding(innerPadding),
                viewModel = feedViewModel,
                userId = userId,
                sessionId = sessionId
            )
            MainTab.CREATEPOST -> CreatePostScreen(
                modifier = Modifier.padding(innerPadding),
                onBackToFeed = { viewModel.changeTab(MainTab.FEED) },

            )
            MainTab.USERPROFILE -> UserProfileScreen(
                modifier = Modifier.padding(innerPadding),
                userId = userId,
                sessionId = sessionId

            )
        }
    }
}