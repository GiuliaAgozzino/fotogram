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

class MyUserProfileViewModel(
    private val userId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var showError by mutableStateOf(false)
        private set

    var userInfo by mutableStateOf<User?>(null)
        private set

    // Post completi
    var userPosts by mutableStateOf<List<Post>>(emptyList())
        private set

    var isLoadingPosts by mutableStateOf(false)
        private set

    var hasMorePosts by mutableStateOf(true)
        private set

    // Edit dialog
    var showEditDialog by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    private var currentMaxPostId: Int = 0
    private val pageSize = 10

    init {
        loadUserInfo()
    }

    fun loadUserInfo(forceRefresh: Boolean = false) {
        if (userId == null) return

        viewModelScope.launch {
            isLoading = true
            showError = false

            try {
                val result = apiRepository.getUserInfo(sessionId, userId, forceRefresh)

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
        if (userId == null || isLoadingPosts || !hasMorePosts) return

        viewModelScope.launch {
            isLoadingPosts = true

            try {
                val result = apiRepository.getUserPostIds(sessionId, userId, currentMaxPostId)

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

    fun updateProfile(name: String, bio: String, dateOfBirth: String, newPicture: String?) {
        viewModelScope.launch {
            isSaving = true

            try {
                val result = apiRepository.updateProfile(sessionId, name, bio, dateOfBirth, newPicture)

                if (result.isSuccess) {
                    userInfo = result.getOrNull()
                    showEditDialog = false
                } else {
                    showError = true
                }
            } catch (e: Exception) {
                showError = true
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
        loadUserInfo(forceRefresh = true)
    }
}