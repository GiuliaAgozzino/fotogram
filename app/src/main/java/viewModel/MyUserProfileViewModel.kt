package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import model.User
import repository.ApiRepository

class MyUserProfileViewModel(
    private val userId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    // Stati UI - Info utente
    var isLoading by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var userInfo by mutableStateOf<User?>(null)
        private set

    // Stati UI - Modifica profilo
    var isSaving by mutableStateOf(false)
        private set

    var showEditDialog by mutableStateOf(false)
        private set

    // Stati UI - Post (lista di ID)
    var userPostIds by mutableStateOf<List<Int>>(emptyList())
        private set

    var isLoadingPosts by mutableStateOf(false)
        private set

    var hasMorePosts by mutableStateOf(true)
        private set

    // Paginazione
    private var currentMaxPostId: Int = 0
    private val pageSize = 10

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
                    loadMorePosts()
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

    fun loadMorePosts() {
        if (userId == null || isLoadingPosts || !hasMorePosts) return

        viewModelScope.launch {
            isLoadingPosts = true

            try {
                val result = apiRepository.getUserPostIds(sessionId, userId, currentMaxPostId)

                if (result.isSuccess) {
                    val newIds = result.getOrNull() ?: emptyList()
                    Log.d("MyUserProfileViewModel", "Ricevuti ${newIds.size} post IDs")

                    when {
                        newIds.isEmpty() -> {
                            hasMorePosts = false
                        }
                        newIds.size < pageSize -> {
                            userPostIds = userPostIds + newIds
                            hasMorePosts = false
                        }
                        else -> {
                            userPostIds = userPostIds + newIds
                            currentMaxPostId = newIds.last() - 1
                            if (currentMaxPostId <= 0) {
                                hasMorePosts = false
                            }
                        }
                    }
                } else {
                    showError = true
                    Log.e("MyUserProfileViewModel", "Errore caricamento post", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                showError = true
                Log.e("MyUserProfileViewModel", "Eccezione", e)
            } finally {
                isLoadingPosts = false
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
                    userInfo = result.getOrNull()
                    closeEditDialog()
                    Log.d("MyUserProfileViewModel", "Profilo aggiornato")
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
        userPostIds = emptyList()
        currentMaxPostId = 0
        hasMorePosts = true
        loadUserInfo()
    }
}
