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

class UserProfileViewModel(
    private val targetUserId: Int?,     // L'utente da visualizzare
    private val sessionId: String?,     // Sessione dell'utente loggato
    private val apiRepository: ApiRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var userInfo by mutableStateOf<UserResponse?>(null)
        private set

    // Stato per il follow
    var isFollowLoading by mutableStateOf(false)
        private set

    // Post dell'utente
    var userPosts by mutableStateOf<List<PostWithAuthor>>(emptyList())
        private set

    var isLoadingPosts by mutableStateOf(false)
        private set

    private var currentMaxPostId: Int = 0

    fun loadUserInfo() {
        if (targetUserId == null) return

        viewModelScope.launch {
            isLoading = true
            showError = false

            try {
                val result = apiRepository.getUserInfo(sessionId, targetUserId)

                if (result.isSuccess) {
                    userInfo = result.getOrNull()
                    Log.d("UserProfileViewModel", "Dati utente caricati: ${userInfo?.username}")

                    // Dopo aver caricato le info, carica i post
                    //loadUserPosts()
                } else {
                    showError = true
                    Log.e("UserProfileViewModel", "Errore recupero dati", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                showError = true
                Log.e("UserProfileViewModel", "Eccezione", e)
            } finally {
                isLoading = false
            }
        }
    }

    /*private fun loadUserPosts() {
        if (targetUserId == null || isLoadingPosts) return

        viewModelScope.launch {
            isLoadingPosts = true

            try {
                val result = apiRepository.getUserPosts(
                    sessionId = sessionId,
                    userId = targetUserId,
                    maxPostId = currentMaxPostId
                )

                if (result.isSuccess) {
                    val newPosts = result.getOrNull() ?: emptyList()

                    if (newPosts.isNotEmpty()) {
                        userPosts = userPosts + newPosts
                        currentMaxPostId = newPosts.last().postId - 1
                        Log.d("UserProfileViewModel", "Caricati ${newPosts.size} post")
                    }
                } else {
                    Log.e("UserProfileViewModel", "Errore caricamento post", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Eccezione caricamento post", e)
            } finally {
                isLoadingPosts = false
            }
        }
    }*/

    fun toggleFollow() {
        if (targetUserId == null || isFollowLoading) return

        val currentlyFollowing = userInfo?.isYourFollowing ?: false

        viewModelScope.launch {
            isFollowLoading = true

            try {
                // Chiama l'API appropriata
                val result: Result<Unit> = if (currentlyFollowing) {
                    apiRepository.unfollowUser(sessionId, targetUserId)
                } else {
                    apiRepository.followUser(sessionId, targetUserId)
                }

                // Controlla il risultato
                result.onSuccess {
                    // Aggiorna lo stato locale
                    userInfo = userInfo?.copy(
                        isYourFollowing = !currentlyFollowing,
                        followersCount = (userInfo?.followersCount?: 0) + if (currentlyFollowing) -1 else 1
                    )
                    Log.d("UserProfileViewModel", "Follow toggle: ora following = ${!currentlyFollowing}")
                }.onFailure { exception ->
                    showError = true
                    Log.e("UserProfileViewModel", "Errore follow/unfollow", exception)
                }
            } catch (e: Exception) {
                showError = true
                Log.e("UserProfileViewModel", "Eccezione follow/unfollow", e)
            } finally {
                isFollowLoading = false
            }
        }
    }

   /* fun loadMorePosts() {
        loadUserPosts()
    }*/

    fun clearError() {
        showError = false
    }

    fun refresh() {
        userPosts = emptyList()
        currentMaxPostId = 0
        loadUserInfo()
    }
}