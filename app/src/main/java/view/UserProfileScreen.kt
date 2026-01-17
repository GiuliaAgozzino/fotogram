package view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
    onBackClick: () -> Unit
) {
    var fullscreenImage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    // Carica i dati all'avvio
    LaunchedEffect(Unit) {
        userProfileViewModel.loadUserInfo()
    }

    // Infinite scroll per i post
    val lastVisibleIndex = remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
    }

    LaunchedEffect(lastVisibleIndex.value) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val total = listState.layoutInfo.totalItemsCount
        val threshold = total - 3

        if (lastVisible != null && lastVisible >= threshold && userProfileViewModel.hasMorePosts) {
            Log.d("UserProfileScreen", "Trigger loadMore - lastVisible=$lastVisible, total=$total")
            userProfileViewModel.loadMorePosts()
        }
    }

    // Dialog errore
    if (userProfileViewModel.showError) {
        ErrorDialog(
            onDismiss = { userProfileViewModel.clearError() },
            onRetry = { userProfileViewModel.refresh() }
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

        when {
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
                            onFollowClick = {
                                userProfileViewModel.toggleFollow()
                            }
                        )
                    }

                    // Post dell'utente
                    items(
                        items = userProfileViewModel.userPosts,
                        key = { post -> post.postId }
                    ) { post ->
                        PostItem(
                            post = post,
                            isOwnPost = false,
                            isAuthorClickable = false,
                            onAuthorClick = { },
                            onLocationClick = { /* TODO: navigate to map */ },
                            onImageClick = { imageBase64 ->
                                fullscreenImage = imageBase64
                            }
                        )
                    }

                    // Footer: loading o fine post
                    item(key = "footer") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                userProfileViewModel.isLoadingPosts -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                !userProfileViewModel.hasMorePosts && userProfileViewModel.userPosts.isNotEmpty() -> {
                                    Text(
                                        text = "Hai visto tutti i post 🎉",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                !userProfileViewModel.hasMorePosts && userProfileViewModel.userPosts.isEmpty() -> {
                                    Text(
                                        text = "Nessun post pubblicato",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}