package model


import kotlinx.serialization.Serializable



//  Aggiorna info utente
@Serializable
data class UpdateUserRequest(
    val username: String,
    val bio: String? = null,
    val dateOfBirth: String? = null
)
//  Aggiorna immagine profilo
@Serializable
data class UpdateImageRequest(
    val base64: String
)


// RESPONSE MODELS (dati ricevuti dal server)


// Risposta creazione utente
@Serializable
data class CreateUserResponse(
    val sessionId: String,
    val userId: Int
)

// Risposta aggiornamento
@Serializable
data class UserResponse(
    val id: Int,
    val createdAt: String,
    val username: String? = " utente sconosciuto",
    val bio: String? = null,
    val dateOfBirth: String? = null,
    val profilePicture: String? = null,
    val isYourFollower: Boolean = false,
    val isYourFollowing: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0
)
//Risposta post utente
@Serializable
data class LocationResponse(
    val latitude: Double?,
    val longitude: Double?
)
@Serializable
data class NewPostRequest(
    val contentText: String,
    val contentPicture: String,
    val location: LocationResponse? = null
)

@Serializable
data class PostResponse(
    val id: Int,
    val authorId: Int,
    val createdAt: String,
    val contentPicture: String,
    val contentText: String? = null,
    val location: LocationResponse? = null
)
// Model combinato per la UI
data class PostWithAuthor(
    val postId: Int,
    val authorId: Int,
    val authorName: String? = "utente sconosciuto",
    val authorPicture: String?,
    val isFollowing: Boolean,
    val contentPicture: String,
    val contentText: String?,
    val location: LocationResponse? = null
)


