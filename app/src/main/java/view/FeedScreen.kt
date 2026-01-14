package view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.FeedViewModel
import view.common.ErrorDialog
import view.common.LoadingIndicator
import view.common.PostItem
import view.common.FullscreenImageDialog

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    feedViewModel: FeedViewModel,
    onNavigateToProfile: (userId: Int) -> Unit = {},
    onNavigateToMap: (postId: Int) -> Unit = {}
) {
    var fullscreenImage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

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

    // Infinite scroll - derivedStateOf per performance
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            // Carica quando siamo a 3 elementi dalla fine
            val threshold = 3

            totalItems > 0 &&
                    lastVisibleIndex >= totalItems - threshold &&
                    feedViewModel.hasMorePosts &&
                    !feedViewModel.isLoadingMore &&
                    !feedViewModel.isLoading
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            Log.d("FeedScreen", "🔄 Trigger loadMore")
            feedViewModel.loadMorePosts()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            feedViewModel.isLoading && feedViewModel.posts.isEmpty() -> {
                LoadingIndicator()
            }

            feedViewModel.posts.isEmpty() && !feedViewModel.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Nessun post disponibile")
                        Button(onClick = { feedViewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ricarica")
                        }
                    }
                }
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

                    // Indicatore di caricamento in fondo
                    if (feedViewModel.isLoadingMore) {
                        item(key = "loading_indicator") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Messaggio fine post
                    if (!feedViewModel.hasMorePosts && feedViewModel.posts.isNotEmpty() && !feedViewModel.isLoadingMore) {
                        item(key = "end_message") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Hai visto tutti i post!",
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