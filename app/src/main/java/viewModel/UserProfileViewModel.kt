package viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import model.UserResponse
import repository.ApiRepository

class UserProfileViewModel(
    private val userId: Int?,
    private val sessionId: String?,
    private val apiRepository: ApiRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var userInfo by mutableStateOf<UserResponse?>(null)
        private set

    // State per il dialog di modifica
    var showEditDialog by mutableStateOf(false)
        private set

    init {
        loadUserInfo()
    }
    fun loadUserInfo() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val result = apiRepository.getUserInfo(sessionId, userId)

                if (result.isSuccess) {
                    val response = result.getOrNull()!!

                    userInfo = UserResponse(
                        id = response.id,
                        createdAt = response.createdAt,
                        username = response.username,
                        bio = response.bio,
                        dateOfBirth = response.dateOfBirth,
                        profilePicture = response.profilePicture,
                        isYourFollower = response.isYourFollower,
                        isYourFollowing = response.isYourFollowing,
                        followersCount = response.followersCount,
                        followingCount = response.followingCount,
                        postCount = response.postCount
                    )

                    Log.d("UserProfileViewModel", "Dati utente caricati: ${userInfo?.username} ${userInfo?.bio}")
                    Log.d("UserProfileViewModel", "Dati utente caricati:${sessionId}")
                } else {
                    val exception = result.exceptionOrNull()
                    errorMessage = "Errore: ${exception?.message}"
                    Log.e("UserProfileViewModel", "Errore recupero dati utente", exception)
                }

            } catch (e: Exception) {
                errorMessage = "Errore di rete: ${e.message}"
                Log.e("UserProfileViewModel", "Eccezione", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun openEditDialog() {
        showEditDialog = true
    }

    fun closeEditDialog() {
        showEditDialog = false
    }

    fun updateProfile(
        newName: String,
        newBio: String,
        newDateOfBirth: String,
        newPicture: String? = null
    ) {
        viewModelScope.launch {
            isSaving = true
            errorMessage = null

            // Log per vedere cosa stai inviando
            Log.d("UserProfileViewModel", "Invio al server - bio: '$newDateOfBirth', bioConvertita: ${newDateOfBirth}")

            try {
                val result = apiRepository.updateProfile(
                    sessionId = sessionId,
                    username = newName,
                    bio = newBio,
                    dateOfBirth = newDateOfBirth,
                    newPicture = newPicture
                )

                if (result.isSuccess) {
                    // Log per vedere cosa restituisce il server
                    val response = result.getOrNull()
                    Log.d("UserProfileViewModel", "Risposta server - bio: '${response?.dateOfBirth}', date: '${response?.dateOfBirth}'")

                    closeEditDialog()
                    loadUserInfo()

                } else {
                    val exception = result.exceptionOrNull()
                    errorMessage = "Errore: ${exception?.message}"
                    Log.e("UserProfileViewModel", "Errore aggiornamento profilo", exception)
                }

            } catch (e: Exception) {
                errorMessage = "Errore di rete: ${e.message}"
                Log.e("UserProfileViewModel", "Eccezione", e)
            } finally {
                isSaving = false
            }
        }
    }

    fun refresh() {
        loadUserInfo()
    }
}