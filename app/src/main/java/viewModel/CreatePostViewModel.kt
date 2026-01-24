package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import model.LocationData
import model.Post
import repository.ApiRepository

class CreatePostViewModel(
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var postCreated by mutableStateOf(false)
        private set

    // Post appena creato (per aggiungerlo al DataViewModel)
    var createdPost by mutableStateOf<Post?>(null)
        private set

    fun newPost(
        text: String,
        picture: String,
        location: LocationData? = null
    ) {
        viewModelScope.launch {
            isLoading = true
            showError = false

            try {
                val result = apiRepository.newPost(
                    sessionId = sessionId,
                    contentText = text,
                    contentPicture = picture,
                    location = location
                )

                if (result.isSuccess) {
                    createdPost = result.getOrNull()
                    postCreated = true
                    Log.d("CreatePostViewModel", "Post creato con successo: ${createdPost?.id}")
                } else {
                    showError = true
                    Log.e("CreatePostViewModel", "Errore creazione", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                showError = true
                Log.e("CreatePostViewModel", "Eccezione", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        showError = false
    }

    fun resetPostCreated() {
        postCreated = false
        createdPost = null
    }
}
