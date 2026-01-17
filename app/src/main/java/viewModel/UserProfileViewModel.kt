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
    private val targetUserId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 10
    }

    var isLoading by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var userInfo by mutableStateOf<UserResponse?>(null)
        private set

    var isFollowLoading by mutableStateOf(false)
        private set

    // Post dell'utente
    var userPosts by mutableStateOf<List<PostWithAuthor>>(emptyList())
        private set

    var isLoadingPosts by mutableStateOf(false)
        private set

    var hasMorePosts by mutableStateOf(true)
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

                    //  carico i post
                    loadUserPosts()
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

    fun loadUserPosts() {
        if (targetUserId == null || isLoadingPosts || !hasMorePosts) return

        viewModelScope.launch {
            isLoadingPosts = true

            try {
                val result = apiRepository.getUserPosts(
                    sessionId = sessionId,
                    authorId = targetUserId,
                    maxPostId = currentMaxPostId
                )

                if (result.isSuccess) {
                    val newPosts = result.getOrNull() ?: emptyList()

                    Log.d("UserProfileViewModel", "Ricevuti ${newPosts.size} post")

                    when {
                        newPosts.isEmpty() -> {
                            hasMorePosts = false
                            Log.d("UserProfileViewModel", "Fine post: lista vuota")
                        }
                        newPosts.size < PAGE_SIZE -> {
                            userPosts = userPosts + newPosts
                            hasMorePosts = false
                            Log.d("UserProfileViewModel", "Fine post: ricevuti ${newPosts.size} < $PAGE_SIZE")
                        }
                        else -> {
                            userPosts = userPosts + newPosts
                            currentMaxPostId = newPosts.last().postId - 1

                            if (currentMaxPostId!! <= 0) {
                                hasMorePosts = false
                                Log.d("UserProfileViewModel", "Fine post: maxPostId <= 0")
                            }

                            Log.d("UserProfileViewModel", "Caricati ${newPosts.size} post. Totale: ${userPosts.size}")
                        }
                    }
                } else {
                    showError = true
                    Log.e("UserProfileViewModel", "Errore: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showError = true
                Log.e("UserProfileViewModel", "Eccezione caricamento post", e)
            } finally {
                isLoadingPosts = false
            }
        }
    }

    fun toggleFollow() {
        if (targetUserId == null || isFollowLoading) return

        val currentlyFollowing = userInfo?.isYourFollowing ?: false

        viewModelScope.launch {
            isFollowLoading = true

            try {
                val result: Result<Unit> = if (currentlyFollowing) {
                    apiRepository.unfollowUser(sessionId, targetUserId)
                } else {
                    apiRepository.followUser(sessionId, targetUserId)
                }

                result.onSuccess {
                    userInfo = userInfo?.copy(
                        isYourFollowing = !currentlyFollowing,
                        followersCount = (userInfo?.followersCount ?: 0) + if (currentlyFollowing) -1 else 1
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

    fun loadMorePosts() {
        loadUserPosts()
    }

    fun clearError() {
        showError = false
    }

    fun refresh() {
        userPosts = emptyList()
        currentMaxPostId = 0
        hasMorePosts = true
        loadUserInfo()
    }
}