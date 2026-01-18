package view.common


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.PostWithAuthor


@Composable
fun InfiniteScrollEffect(
    listState: LazyListState,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    threshold: Int = 3,
    tag: String = "InfiniteScroll"
) {
    val lastVisibleIndex = remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
    }

    LaunchedEffect(lastVisibleIndex.value) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val total = listState.layoutInfo.totalItemsCount
        val triggerThreshold = total - threshold

        if (lastVisible != null && lastVisible >= triggerThreshold && hasMore) {
            Log.d(tag, "Trigger loadMore - lastVisible=$lastVisible, total=$total")
            onLoadMore()
        }
    }
}


@Composable
fun PostListFooter(
    isLoading: Boolean,
    hasMore: Boolean,
    isEmpty: Boolean,
    emptyMessage: String = "Nessun post pubblicato",
    endMessage: String = "Hai visto tutti i post 🎉"
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
                    text = endMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            !hasMore && isEmpty -> {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


fun LazyListScope.postItemsWithFooter(
    posts: List<PostWithAuthor>,
    currentUserId: Int? = null,
    isAuthorClickable: Boolean = true,
    isLoading: Boolean,
    hasMore: Boolean,
    emptyMessage: String = "Nessun post pubblicato",
    endMessage: String = "Hai visto tutti i post 🎉",
    onAuthorClick: (Int) -> Unit = {},
    onLocationClick: (Int) -> Unit = {},
    onImageClick: (String) -> Unit = {}
) {
    items(
        items = posts,
        key = { it.postId }
    ) { post ->
        PostItem(
            post = post,
            isOwnPost = currentUserId?.let { post.authorId == it } ?: false,
            isAuthorClickable = isAuthorClickable,
            onAuthorClick = onAuthorClick,
            onLocationClick = onLocationClick,
            onImageClick = onImageClick
        )
    }

    item(key = "footer") {
        PostListFooter(
            isLoading = isLoading,
            hasMore = hasMore,
            isEmpty = posts.isEmpty(),
            emptyMessage = emptyMessage,
            endMessage = endMessage
        )
    }
}

