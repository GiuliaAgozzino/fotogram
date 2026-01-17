package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import model.PostWithAuthor
import model.UserResponse
import repository.ApiRepository

class MyUserProfileViewModel(
    private val userId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 10
    }

    var isLoading by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var userInfo by mutableStateOf<UserResponse?>(null)
        private set

    var showEditDialog by mutableStateOf(false)
        private set

    // Post dell'utente
    var userPosts by mutableStateOf<List<PostWithAuthor>>(emptyList())
        private set

    var isLoadingPosts by mutableStateOf(false)
        private set

    var hasMorePosts by mutableStateOf(true)
        private set

    private var currentMaxPostId: Int =0

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        if (userId == null) return

        viewModelScope.launch {
            isLoading = true
            showError = false

            try {
                val result = apiRepository.getUserInfo(sessionId, userId)

                if (result.isSuccess) {
                    userInfo = result.getOrNull()
                    Log.d("MyUserProfileViewModel", "Dati utente caricati: ${userInfo?.username}")

                    // Carica i post dopo le info
                    loadUserPosts()
                } else {
                    showError = true
                    Log.e("MyUserProfileViewModel", "Errore recupero dati", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                showError = true
                Log.e("MyUserProfileViewModel", "Eccezione", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadUserPosts() {
        if (userId == null || isLoadingPosts || !hasMorePosts) return

        viewModelScope.launch {
            isLoadingPosts = true

            try {
                val result = apiRepository.getUserPosts(
                    sessionId = sessionId,
                    authorId = userId,
                    maxPostId = currentMaxPostId
                )

                if (result.isSuccess) {
                    val newPosts = result.getOrNull() ?: emptyList()

                    Log.d("MyUserProfileViewModel", "Ricevuti ${newPosts.size} post")

                    when {
                        newPosts.isEmpty() -> {
                            hasMorePosts = false
                            Log.d("MyUserProfileViewModel", "Fine post: lista vuota")
                        }
                        newPosts.size < PAGE_SIZE -> {
                            userPosts = userPosts + newPosts
                            hasMorePosts = false
                            Log.d("MyUserProfileViewModel", "Fine post: ricevuti ${newPosts.size} < $PAGE_SIZE")
                        }
                        else -> {
                            userPosts = userPosts + newPosts
                            currentMaxPostId = newPosts.last().postId - 1

                            if (currentMaxPostId!! <= 0) {
                                hasMorePosts = false
                            }

                            Log.d("MyUserProfileViewModel", "Caricati ${newPosts.size} post. Totale: ${userPosts.size}")
                        }
                    }
                } else {
                    showError = true
                    Log.e("MyUserProfileViewModel", "Errore: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showError = true
                Log.e("MyUserProfileViewModel", "Eccezione caricamento post", e)
            } finally {
                isLoadingPosts = false
            }
        }
    }

    fun loadMorePosts() {
        loadUserPosts()
    }

    fun updateProfile(
        newName: String,
        newBio: String,
        newDateOfBirth: String,
        newPicture: String? = null
    ) {
        viewModelScope.launch {
            isSaving = true
            showError = false

            try {
                val result = apiRepository.updateProfile(
                    sessionId = sessionId,
                    username = newName,
                    bio = newBio,
                    dateOfBirth = newDateOfBirth,
                    newPicture = newPicture
                )

                if (result.isSuccess) {
                    closeEditDialog()
                    refresh()
                } else {
                    showError = true
                    Log.e("MyUserProfileViewModel", "Errore aggiornamento", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                showError = true
                Log.e("MyUserProfileViewModel", "Eccezione", e)
            } finally {
                isSaving = false
            }
        }
    }

    fun openEditDialog() { showEditDialog = true }
    fun closeEditDialog() { showEditDialog = false }
    fun clearError() { showError = false }

    fun refresh() {
        userPosts = emptyList()
        currentMaxPostId = 0
        hasMorePosts = true
        loadUserInfo()
    }
}