package viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import model.PostWithAuthor
import repository.ApiRepository
import utils.PostsPaginator


class FeedViewModel(
    private val userId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    // Stati UI
    var posts by mutableStateOf<List<PostWithAuthor>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var hasMorePosts by mutableStateOf(true)
        private set

    // Stato scroll per ripristinare la posizione
    var firstVisibleItemIndex by mutableStateOf(0)
        private set

    var firstVisibleItemScrollOffset by mutableStateOf(0)
        private set

    // Paginator per la gestione del caricamento
    private val paginator = PostsPaginator(
        tag = "FeedViewModel",
      //  initialMaxPostId = 11, per vedere solo i post con id <= 10
        coroutineScope = viewModelScope,
        onLoadingChange = { isLoading = it },
        onPostsChange = { posts = it },
        onHasMoreChange = { hasMorePosts = it },
        onError = { showError = true },
        fetchPosts = { maxPostId ->
            apiRepository.getUserFeed(sessionId, maxPostId = maxPostId)
        }
    )

    init {
        paginator.loadMore()
    }


    fun fetchNewPosts() {
        paginator.loadMore()
    }


    fun refresh() {
        if (isRefreshing) return

        viewModelScope.launch {
            isRefreshing = true
            paginator.reset()
            paginator.loadMore()
            isRefreshing = false
        }
    }


    fun saveScrollState(index: Int, offset: Int) {
        firstVisibleItemIndex = index
        firstVisibleItemScrollOffset = offset
    }


    fun clearError() {
        showError = false
    }
}