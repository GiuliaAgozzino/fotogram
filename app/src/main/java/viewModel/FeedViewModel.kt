package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import repository.ApiRepository


class FeedViewModel(
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    // Lista degli ID dei post nel feed
    var feedPostIds by mutableStateOf<List<Int>>(emptyList())
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

    // Ultimo ID caricato per paginazione
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

                    when {
                        newIds.isEmpty() -> {
                            hasMorePosts = false
                        }
                        newIds.size < pageSize -> {
                            feedPostIds = feedPostIds + newIds
                            hasMorePosts = false
                        }
                        else -> {
                            feedPostIds = feedPostIds + newIds
                            currentMaxPostId = newIds.last() - 1
                            if (currentMaxPostId <= 0) {
                                hasMorePosts = false
                            }
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


    fun refresh() {
        if (isRefreshing) return

        viewModelScope.launch {
            isRefreshing = true
            feedPostIds = emptyList()
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
