package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import model.Post
import model.User
import repository.ApiRepository

class UserProfileViewModel(
    private val targetUserId: Int?,
    private val sessionId: String?,
    private val userId: Int?,  // <-- AGGIUNGI: l'utente corrente
    private val apiRepository: ApiRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var userInfo by mutableStateOf<User?>(null)
        private set

    var isFollowLoading by mutableStateOf(false)
        private set

    var followChanged by mutableStateOf(false)
        private set

    var userPosts by mutableStateOf<List<Post>>(emptyList())
        private set

    var isLoadingPosts by mutableStateOf(false)
        private set

    var hasMorePosts by mutableStateOf(true)
        private set

    private var currentMaxPostId: Int = 0
    private val pageSize = 10

    fun loadUserInfo(forceRefresh: Boolean = false) {
        if (targetUserId == null) return

        viewModelScope.launch {
            isLoading = true
            showError = false

            try {
                val result = apiRepository.getUserInfo(sessionId, targetUserId, forceRefresh)

                if (result.isSuccess) {
                    userInfo = result.getOrNull()
                    if (!forceRefresh) {
                        loadMorePosts()
                    }
                } else {
                    showError = true
                }
            } catch (e: Exception) {
                showError = true
            } finally {
                isLoading = false
            }
        }
    }


    fun loadMorePosts() {
        if (targetUserId == null || isLoadingPosts || !hasMorePosts) return

        viewModelScope.launch {
            isLoadingPosts = true

            try {
                val result = apiRepository.getUserPostIds(sessionId, targetUserId, currentMaxPostId)

                if (result.isSuccess) {
                    val newIds = result.getOrNull() ?: emptyList()

                    if (newIds.isEmpty()) {
                        hasMorePosts = false
                    } else {
                        val newPosts = newIds.mapNotNull { postId ->
                            apiRepository.getPost(sessionId, postId).getOrNull()
                        }

                        userPosts = userPosts + newPosts

                        if (newIds.size < pageSize) {
                            hasMorePosts = false
                        } else {
                            currentMaxPostId = newIds.last() - 1
                            if (currentMaxPostId <= 0) hasMorePosts = false
                        }
                    }
                } else {
                    showError = true
                }
            } catch (e: Exception) {
                showError = true
            } finally {
                isLoadingPosts = false
            }
        }
    }

    fun toggleFollow(onCurrentUserChanged: () -> Unit) {  // <-- Senza User
        if (targetUserId == null || isFollowLoading) return

        val currentlyFollowing = userInfo?.isYourFollowing ?: false

        viewModelScope.launch {
            isFollowLoading = true

            try {
                val result = if (currentlyFollowing) {
                    apiRepository.unfollowUser(sessionId,  targetUserId)
                } else {
                    apiRepository.followUser(sessionId,  targetUserId)
                }

                result.onSuccess {
                    // Ricarica target user dalla cache aggiornata
                    val refreshedTarget = apiRepository.getUserInfo(sessionId, targetUserId)
                    refreshedTarget.onSuccess { user ->
                        userInfo = user
                    }

                    // Notifica che l'utente corrente è cambiato
                    onCurrentUserChanged()

                    followChanged = true
                }.onFailure {
                    showError = true
                }
            } catch (e: Exception) {
                showError = true
            } finally {
                isFollowLoading = false
            }
        }
    }

    fun clearError() { showError = false }
    fun resetFollowChanged() { followChanged = false }

    fun refresh() {
        userPosts = emptyList()
        currentMaxPostId = 0
        hasMorePosts = true
        loadUserInfo(forceRefresh = true)
    }
}