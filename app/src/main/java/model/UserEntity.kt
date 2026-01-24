package model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity (
    @PrimaryKey val id: Int,
    val createdAt: String,
    val username: String,
    val bio: String,
    val dateOfBirth: String,
    val profilePicture: String,
    val isYourFollower: Boolean,
    val isYourFollowing: Boolean,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int
)

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        createdAt = createdAt,
        username = username ?: "utente sconosciuto",
        bio = bio ?: "",
        dateOfBirth = dateOfBirth ?: "",
        profilePicture = profilePicture ?: "",
        isYourFollower = isYourFollower,
        isYourFollowing = isYourFollowing,
        followersCount = followersCount,
        followingCount = followingCount,
        postsCount = postsCount
    )
}

fun UserEntity.toUser(): User {
    return User(
        id = id,
        createdAt = createdAt,
        username = username,
        bio = bio.ifEmpty { null },
        dateOfBirth = dateOfBirth.ifEmpty { null },
        profilePicture = profilePicture.ifEmpty { null },
        isYourFollower = isYourFollower,
        isYourFollowing = isYourFollowing,
        followersCount = followersCount,
        followingCount = followingCount,
        postsCount = postsCount
    )
}