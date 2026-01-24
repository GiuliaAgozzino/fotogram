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

class UserProfileViewModel(
    private val targetUserId: Int?,
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

    // Stati UI - Follow
    var isFollowLoading by mutableStateOf(false)
        private set

    var followChanged by mutableStateOf(false)
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
                    loadMorePosts()
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

    fun loadMorePosts() {
        if (targetUserId == null || isLoadingPosts || !hasMorePosts) return

        viewModelScope.launch {
            isLoadingPosts = true

            try {
                val result = apiRepository.getUserPostIds(sessionId, targetUserId, currentMaxPostId)

                if (result.isSuccess) {
                    val newIds = result.getOrNull() ?: emptyList()
                    Log.d("UserProfileViewModel", "Ricevuti ${newIds.size} post IDs")

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
                    Log.e("UserProfileViewModel", "Errore caricamento post", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                showError = true
                Log.e("UserProfileViewModel", "Eccezione", e)
            } finally {
                isLoadingPosts = false
            }
        }
    }

    fun toggleFollow(dataViewModel: DataViewModel) {
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
                    // Aggiorna userInfo locale
                    userInfo = userInfo?.copy(
                        isYourFollowing = !currentlyFollowing,
                        followersCount = (userInfo?.followersCount ?: 0) + if (currentlyFollowing) -1 else 1
                    )

                    // Aggiorna anche nel DataViewModel condiviso (Single Source of Truth)
                    dataViewModel.updateFollowingStatus(targetUserId, !currentlyFollowing)

                    followChanged = true
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

    fun clearError() {
        showError = false
    }

    fun resetFollowChanged() {
        followChanged = false
    }

    fun refresh() {
        userPostIds = emptyList()
        currentMaxPostId = 0
        hasMorePosts = true
        loadUserInfo()
    }
}
