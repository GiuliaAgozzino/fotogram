package view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.FeedViewModel
import view.common.CommonDialogs
import view.common.InfiniteScrollEffect
import view.common.LoadingIndicator
import view.common.postItemsWithFooter

/**
 * Schermata del Feed principale.
 * Mostra i post degli utenti seguiti con pull-to-refresh e paginazione.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    feedViewModel: FeedViewModel,
    currentUserId: Int,
    onNavigateToProfile: (userId: Int) -> Unit = {},
    onNavigateToMap: (postId: Int) -> Unit = {},
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
        onLoadMore = { feedViewModel.fetchNewPosts() },
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
            feedViewModel.isLoading && feedViewModel.posts.isEmpty() -> {
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
                    postItemsWithFooter(
                        posts = feedViewModel.posts,
                        currentUserId = currentUserId,
                        isAuthorClickable = true,
                        isLoading = feedViewModel.isLoading,
                        hasMore = feedViewModel.hasMorePosts,
                        onAuthorClick = onNavigateToProfile,
                        onLocationClick = onNavigateToMap,
                        onImageClick = { fullscreenImage = it }
                    )
                }
            }
        }
    }
}