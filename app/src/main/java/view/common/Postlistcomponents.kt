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

/**
 * Gestisce l'infinite scroll per una LazyColumn.
 * Triggera il caricamento quando l'utente si avvicina alla fine della lista.
 *
 * @param listState Lo stato della LazyColumn
 * @param hasMore Se ci sono altri elementi da caricare
 * @param onLoadMore Callback per caricare altri elementi
 * @param threshold Numero di elementi dalla fine per triggerare il caricamento (default 3)
 * @param tag Tag per i log di debug
 */
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

/**
 * Footer standard per liste di post.
 * Mostra un indicatore di caricamento, un messaggio di fine lista, o un messaggio di lista vuota.
 *
 * @param isLoading Se è in corso un caricamento
 * @param hasMore Se ci sono altri post da caricare
 * @param isEmpty Se la lista è vuota
 * @param emptyMessage Messaggio da mostrare quando la lista è vuota
 * @param endMessage Messaggio da mostrare quando si è arrivati alla fine
 */
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

/**
 * Extension function per aggiungere una lista di post con footer a una LazyColumn.
 * Semplifica l'aggiunta di post items + footer in diversi screen.
 *
 * @param posts Lista dei post da mostrare
 * @param currentUserId ID dell'utente corrente (per determinare isOwnPost)
 * @param isAuthorClickable Se l'autore del post è cliccabile
 * @param isLoading Se è in corso un caricamento
 * @param hasMore Se ci sono altri post da caricare
 * @param emptyMessage Messaggio da mostrare quando la lista è vuota
 * @param endMessage Messaggio da mostrare quando si è arrivati alla fine
 * @param onAuthorClick Callback quando si clicca sull'autore
 * @param onLocationClick Callback quando si clicca sulla posizione
 * @param onImageClick Callback quando si clicca sull'immagine
 */
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

/**
 * Versione semplificata di postItemsWithFooter per profili (autore non cliccabile).
 */
fun LazyListScope.profilePostItems(
    posts: List<PostWithAuthor>,
    isOwnProfile: Boolean,
    isLoading: Boolean,
    hasMore: Boolean,
    onLocationClick: (Int) -> Unit = {},
    onImageClick: (String) -> Unit = {}
) {
    postItemsWithFooter(
        posts = posts,
        currentUserId = if (isOwnProfile) posts.firstOrNull()?.authorId else null,
        isAuthorClickable = false,
        isLoading = isLoading,
        hasMore = hasMore,
        onAuthorClick = { },
        onLocationClick = onLocationClick,
        onImageClick = onImageClick
    )
}