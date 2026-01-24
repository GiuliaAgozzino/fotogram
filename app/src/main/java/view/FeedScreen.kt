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
import model.Post
import model.User
import view.common.CommonDialogs
import view.common.InfiniteScrollEffect
import view.common.LoadingIndicator
import view.common.PostItem
import viewModel.DataViewModel
import viewModel.FeedViewModel

/**
 * Schermata del Feed principale.
 * Usa l'approccio V2 delle slide:
 * - feedViewModel gestisce la lista degli ID
 * - dataViewModel gestisce le mappe di Post e User (condiviso)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    feedViewModel: FeedViewModel,
    dataViewModel: DataViewModel,
    currentUserId: Int,
    onNavigateToProfile: (userId: Int) -> Unit = {}
) {
    // Stato per immagine fullscreen
    var fullscreenImage by remember { mutableStateOf<String?>(null) }

    // Stato della lista con posizione salvata
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = feedViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = feedViewModel.firstVisibleItemScrollOffset
    )

    // Salva posizione scroll quando cambia
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        feedViewModel.saveScrollState(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }

    // Gestione infinite scroll
    InfiniteScrollEffect(
        listState = listState,
        hasMore = feedViewModel.hasMorePosts,
        onLoadMore = { feedViewModel.loadFeed() },
        tag = "FeedScreen"
    )

    // Dialogs (errore + fullscreen image)
    CommonDialogs(
        showError = feedViewModel.showError,
        onDismissError = { feedViewModel.clearError() },
        onRetry = { feedViewModel.refresh() },
        fullscreenImage = fullscreenImage,
        onDismissFullscreen = { fullscreenImage = null }
    )

    // Contenuto principale con pull-to-refresh
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = feedViewModel.isRefreshing,
        onRefresh = { feedViewModel.refresh() }
    ) {
        when {
            // Loading iniziale
            feedViewModel.isLoading && feedViewModel.feedPostIds.isEmpty() -> {
                LoadingIndicator()
            }
            // Lista post
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = feedViewModel.feedPostIds,
                        key = { it }
                    ) { postId ->
                        FeedItem(
                            postId = postId,
                            dataViewModel = dataViewModel,
                            currentUserId = currentUserId,
                            onAuthorClick = onNavigateToProfile,
                            onImageClick = { fullscreenImage = it }
                        )
                    }

                    // Footer
                    item(key = "footer") {
                        FeedFooter(
                            isLoading = feedViewModel.isLoading,
                            hasMore = feedViewModel.hasMorePosts,
                            isEmpty = feedViewModel.feedPostIds.isEmpty()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Singolo elemento del feed.
 * Carica i dati del post e dell'autore tramite il DataViewModel.
 */
@Composable
fun FeedItem(
    postId: Int,
    dataViewModel: DataViewModel,
    currentUserId: Int,
    onAuthorClick: (Int) -> Unit,
    onImageClick: (String) -> Unit
) {
    // Legge post e autore dalle mappe del DataViewModel
    val post = dataViewModel.posts[postId]
    val author = post?.let { dataViewModel.authors[it.authorId] }

    // Carica i dati se non presenti (approccio V2)
    LaunchedEffect(postId, post) {
        if (post == null) {
            dataViewModel.loadPost(postId)
        }
        if (post != null && author == null) {
            dataViewModel.loadAuthor(post.authorId)
        }
    }

    // Mostra loading o contenuto
    if (post == null || author == null) {
        // Placeholder mentre carica
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
            isOwnPost = post.authorId == currentUserId,
            onAuthorClick = onAuthorClick,
            onImageClick = onImageClick
        )
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
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
            !hasMore && !isEmpty -> {
                Text(
                    text = "Hai visto tutti i post 🎉",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            !hasMore && isEmpty -> {
                Text(
                    text = "Nessun post disponibile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
