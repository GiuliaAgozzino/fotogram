package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import model.PostWithAuthor
import repository.ApiRepository

class FeedViewModel(
    private val userId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var posts by mutableStateOf<List<PostWithAuthor>>(emptyList())
        private set

    var hasMorePosts by mutableStateOf(true)
        private set

    private var currentMaxPostId: Int = 0
    private var loadMoreJob: Job? = null

    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            isLoading = true
            showError = false
            currentMaxPostId = 0
            hasMorePosts = true

            Log.d("FeedViewModel", "Caricamento feed iniziale")

            try {
                val result = apiRepository.getUserFeed(sessionId, maxPostId = 0)

                if (result.isSuccess) {
                    val newPosts = result.getOrNull() ?: emptyList()
                    posts = newPosts

                    if (newPosts.isNotEmpty()) {
                        currentMaxPostId = newPosts.last().postId
                        Log.d("FeedViewModel", "Feed caricato: ${newPosts.size} post, lastId=$currentMaxPostId")
                    }

                    // Se abbiamo ricevuto meno di 10 post, non ce ne sono altri
                    if (newPosts.isEmpty()) {
                        hasMorePosts = false
                        Log.d("FeedViewModel", "Fine post raggiunta (meno di 10 ricevuti)")
                    }
                } else {
                    showError = true
                    Log.e("FeedViewModel", "Errore: ${result.exceptionOrNull()?.message}", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                showError = true
                Log.e("FeedViewModel", "Errore di rete: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun loadMorePosts() {
        // Protezioni multiple
        if (isLoadingMore || isLoading || !hasMorePosts) {
            Log.d("FeedViewModel", "Skip loadMore: isLoadingMore=$isLoadingMore, isLoading=$isLoading, hasMore=$hasMorePosts")
            return
        }

        // Cancella job precedente se ancora attivo
        loadMoreJob?.cancel()

        loadMoreJob = viewModelScope.launch {
            isLoadingMore = true
            Log.d("FeedViewModel", "⬇️ Caricamento post aggiuntivi: maxPostId=$currentMaxPostId")

            try {
                val result = apiRepository.getUserFeed(sessionId, maxPostId = currentMaxPostId)

                if (result.isSuccess) {
                    val newPosts = result.getOrNull() ?: emptyList()

                    if (newPosts.isNotEmpty()) {
                        // Filtra eventuali duplicati
                        val existingIds = posts.map { it.postId }.toSet()
                        val uniqueNewPosts = newPosts.filter { it.postId !in existingIds }

                        if (uniqueNewPosts.isNotEmpty()) {
                            posts = posts + uniqueNewPosts
                            currentMaxPostId = uniqueNewPosts.last().postId
                            Log.d("FeedViewModel", "✅ Caricati ${uniqueNewPosts.size} nuovi post. Totale: ${posts.size}")
                        }

                        // Se abbiamo ricevuto meno di 10 post, non ce ne sono altri
                        if (newPosts.isEmpty()) {
                            hasMorePosts = false
                            Log.d("FeedViewModel", "Fine post raggiunta")
                        }
                    } else {
                        hasMorePosts = false
                        Log.d("FeedViewModel", "Nessun altro post disponibile")
                    }
                } else {
                    Log.e("FeedViewModel", "Errore caricamento: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Errore di rete: ${e.message}", e)
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun clearError() {
        showError = false
    }

    fun refresh() {
        loadMoreJob?.cancel()
        posts = emptyList()
        loadFeed()
    }

    override fun onCleared() {
        super.onCleared()
        loadMoreJob?.cancel()
    }
}