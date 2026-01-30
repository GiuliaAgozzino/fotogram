package view

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
import view.common.*
import viewModel.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel,
    onBackClick: () -> Unit,
    onCurrentUserChanged: () -> Unit = {}
) {
    var fullscreenImage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        userProfileViewModel.loadUserInfo()
    }

    InfiniteScrollEffect(
        listState = listState,
        hasMore = userProfileViewModel.hasMorePosts,
        onLoadMore = { userProfileViewModel.loadMorePosts() },
        tag = "UserProfileScreen"
    )

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
                title = { Text(userProfileViewModel.userInfo?.username ?: "Profilo utente") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { innerPadding ->

        when {
            userProfileViewModel.isLoading && userProfileViewModel.userInfo == null -> {
                Box(
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            userProfileViewModel.userInfo != null -> {
                LazyColumn(
                    state = listState,
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "profile_header") {
                        ProfileHeader(
                            user = userProfileViewModel.userInfo!!,
                            showEditButton = false,
                            showFollowButton = true,
                            isFollowing = userProfileViewModel.userInfo!!.isYourFollowing,
                            isFollowLoading = userProfileViewModel.isFollowLoading,
                            onFollowClick = {
                                userProfileViewModel.toggleFollow {
                                    onCurrentUserChanged()
                                }

                            }
                        )
                    }

                    items(
                        items = userProfileViewModel.userPosts,
                        key = { it.id }
                    ) { post ->
                        PostItem(
                            post = post,
                            author = userProfileViewModel.userInfo!!,
                            isOwnPost = false,
                            isAuthorClickable = false,
                            onAuthorClick = { },
                            onImageClick = { fullscreenImage = it }
                        )
                    }

                    item(key = "footer") {
                        PostListFooter(
                            isLoading = userProfileViewModel.isLoadingPosts,
                            hasMore = userProfileViewModel.hasMorePosts,
                            isEmpty = userProfileViewModel.userPosts.isEmpty()
                        )
                    }
                }
            }
        }
    }
}