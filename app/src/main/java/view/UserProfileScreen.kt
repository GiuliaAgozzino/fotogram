package view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import view.common.ErrorDialog
import view.common.FullscreenImageDialog
import view.common.LoadingIndicator
import view.common.PostItem
import view.common.ProfileHeader
import viewModel.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel,
    onBackClick: () -> Unit,
    onNavigateToUserProfile: (userId: Int) -> Unit = {}
) {
    var fullscreenImage by remember { mutableStateOf<String?>(null) }

    // Carica i dati all'avvio
    LaunchedEffect(Unit) {
        userProfileViewModel.loadUserInfo()
    }

    // Dialog errore
    if (userProfileViewModel.showError) {
        ErrorDialog(
            onDismiss = { userProfileViewModel.clearError() },
            onRetry = { userProfileViewModel.loadUserInfo() }
        )
    }

    // Dialog immagine fullscreen
    fullscreenImage?.let { image ->
        FullscreenImageDialog(
            imageBase64 = image,
            onDismiss = { fullscreenImage = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(userProfileViewModel.userInfo?.username ?: "Profilo utente")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                userProfileViewModel.isLoading -> {
                    LoadingIndicator()
                }

                userProfileViewModel.userInfo != null -> {
                    val user = userProfileViewModel.userInfo!!

                    // Header del profilo con bottone follow/unfollow
                    ProfileHeader(
                        user = user,
                        showEditButton = false,
                        showFollowButton = true,
                        isFollowing = user.isYourFollowing,
                        onFollowClick = {
                            userProfileViewModel.toggleFollow()
                        }
                    )

                }

            }
        }
    }
}