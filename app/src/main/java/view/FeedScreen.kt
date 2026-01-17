package view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import viewModel.FeedViewModel
import view.common.ErrorDialog
import view.common.LoadingIndicator
import view.common.PostItem
import view.common.FullscreenImageDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    feedViewModel: FeedViewModel,
    currentUserId: Int,
    onNavigateToProfile: (userId: Int) -> Unit = {},
    onNavigateToMap: (postId: Int) -> Unit = {},
) {
    var fullscreenImage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = feedViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = feedViewModel.firstVisibleItemScrollOffset
    )

    val currentIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val currentOffset = remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val lastVisibleIndex = remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
    }

    // Salva posizione scroll
    LaunchedEffect(currentIndex.value, currentOffset.value) {
        feedViewModel.saveScrollState(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }

    // Carica altri post
    LaunchedEffect(lastVisibleIndex.value) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val total = listState.layoutInfo.totalItemsCount
        val threshold = total - 3


        if (lastVisible != null && lastVisible >= threshold && feedViewModel.hasMorePosts) {
            Log.d("FeedScreen", "Trigger loadMore - lastVisible=$lastVisible, total=$total")
            feedViewModel.fetchNewPosts()
        }
    }

    // Dialog errore
    if (feedViewModel.showError) {
        ErrorDialog(
            onDismiss = { feedViewModel.clearError() },
            onRetry = { feedViewModel.refresh() }
        )
    }

    // Dialog immagine fullscreen
    fullscreenImage?.let { image ->
        FullscreenImageDialog(
            imageBase64 = image,
            onDismiss = { fullscreenImage = null }
        )
    }

    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = feedViewModel.isRefreshing,
        onRefresh = { feedViewModel.refresh() }
    ) {
        when {
            feedViewModel.isLoading && feedViewModel.posts.isEmpty() -> {
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
                        items = feedViewModel.posts,
                        key = { post -> post.postId }
                    ) { post ->
                        PostItem(
                            post = post,
                            isOwnPost = post.authorId == currentUserId,
                            onAuthorClick = { authorId ->
                                onNavigateToProfile(authorId)
                            },
                            onLocationClick = { postId ->
                                onNavigateToMap(postId)
                            },
                            onImageClick = { imageBase64 ->
                                fullscreenImage = imageBase64
                            }
                        )
                    }


                    item(key = "footer") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                feedViewModel.isLoading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                !feedViewModel.hasMorePosts -> {
                                    Text(
                                        text = "Hai visto tutti i post 🎉",
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