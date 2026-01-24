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
import viewModel.DataViewModel
import viewModel.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel,
    dataViewModel: DataViewModel,
    onBackClick: () -> Unit
) {
    var fullscreenImage by remember { mutableStateOf<String?>(null) }
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

    // Dialogs
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
                    item(key = "profile_header") {
                        ProfileHeader(
                            user = userProfileViewModel.userInfo!!,
                            showEditButton = false,
                            showFollowButton = true,
                            isFollowing = userProfileViewModel.userInfo!!.isYourFollowing,
                            isFollowLoading = userProfileViewModel.isFollowLoading,
                            onFollowClick = { userProfileViewModel.toggleFollow(dataViewModel) }
                        )
                    }

                    items(
                        items = userProfileViewModel.userPostIds,
                        key = { it }
                    ) { postId ->
                        UserProfilePostItem(
                            postId = postId,
                            dataViewModel = dataViewModel,
                            onImageClick = { fullscreenImage = it }
                        )
                    }

                    item(key = "footer") {
                        PostListFooter(
                            isLoading = userProfileViewModel.isLoadingPosts,
                            hasMore = userProfileViewModel.hasMorePosts,
                            isEmpty = userProfileViewModel.userPostIds.isEmpty()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfilePostItem(
    postId: Int,
    dataViewModel: DataViewModel,
    onImageClick: (String) -> Unit
) {
    val post = dataViewModel.posts[postId]
    val author = post?.let { dataViewModel.authors[it.authorId] }

    LaunchedEffect(postId, post) {
        if (post == null) {
            dataViewModel.loadPost(postId)
        }
        if (post != null && author == null) {
            dataViewModel.loadAuthor(post.authorId)
        }
    }

    if (post == null || author == null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    } else {
        PostItem(
            post = post,
            author = author,
            isOwnPost = false,
            isAuthorClickable = false,
            onAuthorClick = { },
            onImageClick = onImageClick
        )
    }
}
