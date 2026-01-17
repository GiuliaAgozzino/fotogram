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

    companion object {
        private const val PAGE_SIZE = 10
    }

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

    var firstVisibleItemIndex by mutableStateOf(0)
        private set
    var firstVisibleItemScrollOffset by mutableStateOf(0)
        private set

    private var currentMaxPostId: Int = 0

    init {
        fetchNewPosts()
    }

    fun fetchNewPosts() {
        if (isLoading || !hasMorePosts) return

        viewModelScope.launch {
            isLoading = true
            Log.d("FeedViewModel", "Caricamento post: maxPostId=$currentMaxPostId, hasMore=$hasMorePosts")

            try {
                val result = apiRepository.getUserFeed(sessionId, maxPostId = currentMaxPostId)

                if (result.isSuccess) {
                    val newPosts = result.getOrNull() ?: emptyList()

                    Log.d("FeedViewModel", "Ricevuti ${newPosts.size} post")

                    when {
                        newPosts.isEmpty() -> {
                            // Nessun post ricevuto
                            hasMorePosts = false
                            Log.d("FeedViewModel", "Fine post: lista vuota")
                        }
                        newPosts.size < PAGE_SIZE -> {
                            // Meno post del previsto
                            posts = posts + newPosts
                            hasMorePosts = false
                            Log.d("FeedViewModel", "Fine post: ricevuti ${newPosts.size} < $PAGE_SIZE")
                        }
                        else -> {
                            posts = posts + newPosts
                            currentMaxPostId = newPosts.last().postId - 1

                            // Se maxPostId diventa <= 0, non ci sono più post
                            if (currentMaxPostId <= 0) {
                                hasMorePosts = false
                                Log.d("FeedViewModel", "Fine post: maxPostId <= 0")
                            }

                            Log.d("FeedViewModel", "Caricati ${newPosts.size} post. Totale: ${posts.size}, nextMaxPostId=$currentMaxPostId")
                        }
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
            hasMorePosts = true
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