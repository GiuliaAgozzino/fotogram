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

    var isLoading by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var post by mutableStateOf<PostWithAuthor?>(null)
        private set

    init {
        loadTestPost()
    }

    private fun loadTestPost() {
        viewModelScope.launch {
            isLoading = true
            showError = false

            Log.d("FeedViewModel", "Caricamento post di test: postId=58")

            try {
                val result = apiRepository.getPostWithAuthor(sessionId, postId = 58)

                if (result.isSuccess) {
                    post = result.getOrNull()
                    Log.d("FeedViewModel", "Post caricato: ${post?.authorName}")
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

    fun clearError() {
        showError = false
    }

    fun refresh() {
        loadTestPost()
    }
}