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

/**
 * ViewModel per la schermata del profilo di un altro utente.
 * Gestisce le info utente, i post e la funzionalità di follow/unfollow.
 */
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

    var userInfo by mutableStateOf<UserResponse?>(null)
        private set

    // Stati UI - Follow
    var isFollowLoading by mutableStateOf(false)
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
        tag = "UserProfileViewModel",
        coroutineScope = viewModelScope,
        onLoadingChange = { isLoadingPosts = it },
        onPostsChange = { userPosts = it },
        onHasMoreChange = { hasMorePosts = it },
        onError = { showError = true },
        fetchPosts = { maxPostId ->
            apiRepository.getUserPosts(
                sessionId = sessionId,
                authorId = targetUserId!!,
                maxPostId = maxPostId
            )
        }
    )

    /**
     * Carica le informazioni dell'utente e poi i suoi post.
     */
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

                    // Carica i post dopo le info
                    paginator.loadMore()
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

    /**
     * Alterna lo stato di follow/unfollow per l'utente.
     */
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

    /**
     * Carica altri post (paginazione).
     */
    fun loadMorePosts() {
        paginator.loadMore()
    }

    /**
     * Chiude il dialog di errore.
     */
    fun clearError() {
        showError = false
    }

    /**
     * Ricarica tutto dall'inizio.
     */
    fun refresh() {
        paginator.reset()
        loadUserInfo()
    }
}