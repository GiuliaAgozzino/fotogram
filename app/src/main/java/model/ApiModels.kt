package model

import kotlinx.serialization.Serializable


@Serializable
data class UpdateUserRequest(
    val username: String,
    val bio: String? = null,
    val dateOfBirth: String? = null
)

@Serializable
data class UpdateImageRequest(
    val base64: String
)

@Serializable
data class NewPostRequest(
    val contentText: String,
    val contentPicture: String,
    val location: LocationData? = null
)

@Serializable
data class CreateUserResponse(
    val sessionId: String,
    val userId: Int
)

@Serializable
data class LocationData(
    val latitude: Double?,
    val longitude: Double?
)


@Serializable
data class Post(
    val id: Int,
    val authorId: Int,
    val createdAt: String,
    val contentPicture: String,
    val contentText: String? = null,
    val location: LocationData? = null
)

@Serializable
data class User(
    val id: Int,
    val createdAt: String = "",
    val username: String? = "utente sconosciuto",
    val bio: String? = null,
    val dateOfBirth: String? = null,
    val profilePicture: String? = null,
    val isYourFollower: Boolean = false,
    val isYourFollowing: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0
)

// ==================== ERROR ====================

@Serializable
data class ErrorResponse(
    val message: String
)
