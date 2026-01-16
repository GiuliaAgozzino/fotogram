package view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import view.common.LoadingIndicator
import view.common.ProfileHeader
import viewModel.MyUserProfileViewModel
import viewModel.UserProfileViewModel

@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel : UserProfileViewModel
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            userProfileViewModel.isLoading -> LoadingIndicator()
            userProfileViewModel.userInfo != null  -> {
                ProfileHeader(
                    user = userProfileViewModel.userInfo!!,
                    showEditButton = false,
                    onFollowClick = { }
                )
            }
            else -> Text("Nessun dato disponibile")
        }
    }
}