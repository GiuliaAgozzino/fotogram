package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import model.Post
import model.User
import repository.ApiRepository

data class FeedPost(
    val post: Post,
    val author: User
)

class FeedViewModel(
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    var feedPosts by mutableStateOf<List<FeedPost>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var hasMorePosts by mutableStateOf(true)
        private set

    var firstVisibleItemIndex by mutableStateOf(0)
        private set

    var firstVisibleItemScrollOffset by mutableStateOf(0)
        private set

    private var currentMaxPostId: Int = 0
    private val pageSize = 10

    init {
        loadFeed()
    }

    fun loadFeed() {
        if (isLoading || !hasMorePosts) return

        viewModelScope.launch {
            isLoading = true
            showError = false

            try {
                val result = apiRepository.getFeedPostIds(sessionId, currentMaxPostId)

                if (result.isSuccess) {
                    val newIds = result.getOrNull() ?: emptyList()
                    Log.d("FeedViewModel", "Ricevuti ${newIds.size} post IDs")

                    if (newIds.isEmpty()) {
                        hasMorePosts = false
                    } else {
                        // Carica i post completi
                        val newFeedPosts = newIds.mapNotNull { postId ->
                            loadFeedPost(postId)
                        }

                        feedPosts = feedPosts + newFeedPosts

                        if (newIds.size < pageSize) {
                            hasMorePosts = false
                        } else {
                            currentMaxPostId = newIds.last() - 1
                            if (currentMaxPostId <= 0) hasMorePosts = false
                        }
                    }
                } else {
                    showError = true
                    Log.e("FeedViewModel", "Errore caricamento feed", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                showError = true
                Log.e("FeedViewModel", "Eccezione", e)
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadFeedPost(postId: Int): FeedPost? {
        val postResult = apiRepository.getPost(sessionId, postId)
        if (postResult.isFailure) return null

        val post = postResult.getOrNull() ?: return null

        val authorResult = apiRepository.getUserInfo(sessionId, post.authorId)
        if (authorResult.isFailure) return null

        val author = authorResult.getOrNull() ?: return null

        return FeedPost(post, author)
    }

    fun refresh() {
        if (isRefreshing) return

        viewModelScope.launch {
            isRefreshing = true
            feedPosts = emptyList()
            currentMaxPostId = 0
            hasMorePosts = true
            loadFeed()
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