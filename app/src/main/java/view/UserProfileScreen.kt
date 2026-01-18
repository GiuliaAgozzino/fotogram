package view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import view.common.CommonDialogs
import view.common.InfiniteScrollEffect
import view.common.LoadingIndicator
import view.common.ProfileHeader
import view.common.postItemsWithFooter
import viewModel.UserProfileViewModel

/**
 * Schermata del profilo di un altro utente.
 * Mostra le info utente, pulsante follow/unfollow e lista post.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel,
    onBackClick: () -> Unit,
    onNavigateToMap: (postId: Int) -> Unit = {}
) {
    // Stato per immagine fullscreen
    var fullscreenImage by remember { mutableStateOf<String?>(null) }

    // Stato della lista
    val listState = rememberLazyListState()

    // Carica i dati all'avvio
    LaunchedEffect(Unit) {
        userProfileViewModel.loadUserInfo()
    }

    // Gestione infinite scroll
    InfiniteScrollEffect(
        listState = listState,
        hasMore = userProfileViewModel.hasMorePosts,
        onLoadMore = { userProfileViewModel.loadMorePosts() },
        tag = "UserProfileScreen"
    )

    // Dialogs (errore + fullscreen image)
    CommonDialogs(
        showError = userProfileViewModel.showError,
        onDismissError = { userProfileViewModel.clearError() },
        onRetry = { userProfileViewModel.refresh() },
        fullscreenImage = fullscreenImage,
        onDismissFullscreen = { fullscreenImage = null }
    )

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

        when {
            // Loading iniziale
            userProfileViewModel.isLoading && userProfileViewModel.userInfo == null -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            // Contenuto profilo
            userProfileViewModel.userInfo != null -> {
                LazyColumn(
                    state = listState,
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header del profilo
                    item(key = "profile_header") {
                        ProfileHeader(
                            user = userProfileViewModel.userInfo!!,
                            showEditButton = false,
                            showFollowButton = true,
                            isFollowing = userProfileViewModel.userInfo!!.isYourFollowing,
                            isFollowLoading = userProfileViewModel.isFollowLoading,
                            onFollowClick = { userProfileViewModel.toggleFollow() }
                        )
                    }

                    // Post dell'utente con footer
                    postItemsWithFooter(
                        posts = userProfileViewModel.userPosts,
                        currentUserId = null, // Non è il proprio profilo
                        isAuthorClickable = false, // Siamo già sul profilo
                        isLoading = userProfileViewModel.isLoadingPosts,
                        hasMore = userProfileViewModel.hasMorePosts,
                        onAuthorClick = { }, // Nessuna azione
                        onLocationClick = onNavigateToMap,
                        onImageClick = { fullscreenImage = it }
                    )
                }
            }
        }
    }
}