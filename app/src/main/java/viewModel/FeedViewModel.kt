package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import model.PostWithAuthor
import repository.ApiRepository

class FeedViewModel(
    private val userId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    var posts by mutableStateOf<List<PostWithAuthor>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    // Scroll state (come il prof)
    var firstVisibleItemIndex by mutableStateOf(0)
        private set
    var firstVisibleItemScrollOffset by mutableStateOf(0)
        private set

    private var currentMaxPostId: Int = 0

    init {
        fetchNewPosts()
    }

    fun fetchNewPosts() {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            Log.d("FeedViewModel", "Caricamento post: maxPostId=$currentMaxPostId")

            try {
                val result = apiRepository.getUserFeed(sessionId, maxPostId = currentMaxPostId)

                if (result.isSuccess) {
                    val newPosts = result.getOrNull() ?: emptyList()

                    if (newPosts.isNotEmpty()) {
                        posts = posts + newPosts
                        currentMaxPostId = newPosts.last().postId - 1
                        Log.d("FeedViewModel", "Caricati ${newPosts.size} post. Totale: ${posts.size}")
                    }
                } else {
                    showError = true
                    Log.e("FeedViewModel", "Errore: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showError = true
                Log.e("FeedViewModel", "Errore di rete: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun refresh() {
        if (isRefreshing) return

        viewModelScope.launch {
            isRefreshing = true
            posts = emptyList()
            currentMaxPostId = 0
            fetchNewPosts()
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