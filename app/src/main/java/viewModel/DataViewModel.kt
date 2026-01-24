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


class DataViewModel(
    private val apiRepository: ApiRepository,
    private val sessionId: String?
) : ViewModel() {

    // ==================== STATE ====================

    // Mappa dei post: postId -> Post
    var posts by mutableStateOf<Map<Int, Post>>(emptyMap())
        private set

    // Mappa degli autori: userId -> User
    var authors by mutableStateOf<Map<Int, User>>(emptyMap())
        private set

    // ==================== POST METHODS ====================


    fun loadPost(postId: Int) {
        if (posts.containsKey(postId)) return

        viewModelScope.launch {
            try {
                val result = apiRepository.getPost(sessionId, postId)
                if (result.isSuccess) {
                    val post = result.getOrNull()!!
                    posts = posts + (post.id to post)
                    Log.d("DataViewModel", "Post $postId caricato")
                }
            } catch (e: Exception) {
                Log.e("DataViewModel", "Errore caricamento post $postId", e)
            }
        }
    }


    fun addPost(post: Post) {
        posts = posts + (post.id to post)
    }

    // ==================== AUTHOR/USER METHODS ====================

    fun loadAuthor(userId: Int) {
        if (authors.containsKey(userId)) return

        viewModelScope.launch {
            try {
                val result = apiRepository.getUserInfo(sessionId, userId)
                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    authors = authors + (user.id to user)
                    Log.d("DataViewModel", "Autore ${user.username} caricato")
                }
            } catch (e: Exception) {
                Log.e("DataViewModel", "Errore caricamento autore $userId", e)
            }
        }
    }


    fun updateAuthor(user: User) {
        authors = authors + (user.id to user)
        Log.d("DataViewModel", "Autore ${user.username} aggiornato")
    }


    fun updateFollowingStatus(userId: Int, isFollowing: Boolean) {
        val currentUser = authors[userId] ?: return
        val updatedUser = currentUser.copy(
            isYourFollowing = isFollowing,
            followersCount = currentUser.followersCount + if (isFollowing) 1 else -1
        )
        authors = authors + (userId to updatedUser)
        Log.d("DataViewModel", "Follow status aggiornato per $userId: $isFollowing")
    }

    fun refreshAuthor(userId: Int) {
        viewModelScope.launch {
            try {
                val result = apiRepository.getUserInfo(sessionId, userId)
                if (result.isSuccess) {
                    val user = result.getOrNull()!!
                    authors = authors + (user.id to user)
                    Log.d("DataViewModel", "Autore ${user.username} refreshato")
                }
            } catch (e: Exception) {
                Log.e("DataViewModel", "Errore refresh autore $userId", e)
            }
        }
    }

    // ==================== UTILITY ====================


    fun clearAll() {
        posts = emptyMap()
        authors = emptyMap()
    }
}
