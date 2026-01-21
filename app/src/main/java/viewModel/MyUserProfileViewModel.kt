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
import utils.PostsPaginator


class MyUserProfileViewModel(
    private val userId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {


    var isLoading by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var userInfo by mutableStateOf<UserResponse?>(null)
        private set

    // Stati UI - Modifica profilo
    var isSaving by mutableStateOf(false)
        private set

    var showEditDialog by mutableStateOf(false)
        private set

    // Stati UI - Post
    var userPosts by mutableStateOf<List<PostWithAuthor>>(emptyList())
        private set

    var isLoadingPosts by mutableStateOf(false)
        private set

    var hasMorePosts by mutableStateOf(true)
        private set

    // Paginator per la gestione del caricamento post
    private val paginator = PostsPaginator(
        tag = "MyUserProfileViewModel",
        coroutineScope = viewModelScope,
        onLoadingChange = { isLoadingPosts = it },
        onPostsChange = { userPosts = it },
        onHasMoreChange = { hasMorePosts = it },
        onError = { showError = true },
        fetchPosts = { maxPostId ->
            apiRepository.getUserPosts(
                sessionId = sessionId,
                authorId = userId!!,
                maxPostId = maxPostId
            )
        }
    )

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
                    paginator.loadMore()
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


    fun loadMorePosts() {
        paginator.loadMore()
    }


    fun openEditDialog() {
        showEditDialog = true
    }


    fun closeEditDialog() {
        showEditDialog = false
    }


    fun clearError() {
        showError = false
    }


    fun refresh() {
        paginator.reset()
        loadUserInfo()
    }
}