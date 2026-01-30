package view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import view.common.CommonDialogs
import view.common.InfiniteScrollEffect
import view.common.LoadingIndicator
import view.common.PostItem
import viewModel.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    feedViewModel: FeedViewModel,
    currentUserId: Int,
    onNavigateToProfile: (userId: Int) -> Unit = {}
) {
    var fullscreenImage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = feedViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = feedViewModel.firstVisibleItemScrollOffset
    )

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        feedViewModel.saveScrollState(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }

    InfiniteScrollEffect(
        listState = listState,
        hasMore = feedViewModel.hasMorePosts,
        onLoadMore = { feedViewModel.loadFeed() },
        tag = "FeedScreen"
    )

    CommonDialogs(
        showError = feedViewModel.showError,
        onDismissError = { feedViewModel.clearError() },
        onRetry = { feedViewModel.refresh() },
        fullscreenImage = fullscreenImage,
        onDismissFullscreen = { fullscreenImage = null }
    )

    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = feedViewModel.isRefreshing,
        onRefresh = { feedViewModel.refresh() }
    ) {
        when {
            feedViewModel.isLoading && feedViewModel.feedPosts.isEmpty() -> {
                LoadingIndicator()
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = feedViewModel.feedPosts,
                        key = { it.post.id }
                    ) { feedPost ->
                        PostItem(
                            post = feedPost.post,
                            author = feedPost.author,
                            isOwnPost = feedPost.post.authorId == currentUserId,
                            onAuthorClick = onNavigateToProfile,
                            onImageClick = { fullscreenImage = it }
                        )
                    }

                    item(key = "footer") {
                        FeedFooter(
                            isLoading = feedViewModel.isLoading,
                            hasMore = feedViewModel.hasMorePosts,
                            isEmpty = feedViewModel.feedPosts.isEmpty()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeedFooter(
    isLoading: Boolean,
    hasMore: Boolean,
    isEmpty: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.size(32.dp))
            !hasMore && !isEmpty -> Text(
                text = "Hai visto tutti i post 🎉",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            !hasMore && isEmpty -> Text(
                text = "Nessun post disponibile",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}