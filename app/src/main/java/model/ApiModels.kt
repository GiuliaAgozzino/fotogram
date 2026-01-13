package model


import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable


// REQUEST MODELS (dati da inviare al server)


// PUT /user - Aggiorna info utente
@Serializable
data class UpdateUserRequest(
    val username: String,
    val bio: String? = null,
    val dateOfBirth: String? = null
)
// PUT /user/image - Aggiorna immagine profilo
@Serializable
data class UpdateImageRequest(
    val base64: String
)


// RESPONSE MODELS (dati ricevuti dal server)


// POST /user - Risposta creazione utente
@Serializable
data class CreateUserResponse(
    val sessionId: String,
    val userId: Int
)

// PUT /user e PUT /user/image - Risposta aggiornamento
@Serializable
data class UserResponse(
    val id: Int,
    val createdAt: String,
    val username: String,
    val bio: String? = null,
    val dateOfBirth: String? = null,
    val profilePicture: String? = null, // Può essere null!
    val isYourFollower: Boolean = false,
    val isYourFollowing: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0
)

// Errore generico
@Serializable
data class ErrorResponse(
    val message: String
)