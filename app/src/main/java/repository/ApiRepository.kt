package repository

import model.CreateUserResponse
import model.PostWithAuthor
import model.UserResponse
import repository.api.FollowApi
import repository.api.PostApi
import repository.api.UserApi

class ApiRepository {

    private val userApi = UserApi()
    private val postApi = PostApi()
    private val followApi = FollowApi()

    // User
    suspend fun register(username: String, pictureBase64: String): Result<CreateUserResponse> {
        return userApi.register(username, pictureBase64)
    }

    suspend fun getUserInfo(sessionId: String?, userId: Int?): Result<UserResponse> {
        return userApi.getUserInfo(sessionId, userId)
    }

    suspend fun updateProfile(
        sessionId: String?,
        username: String,
        bio: String?,
        dateOfBirth: String?,
        newPicture: String?
    ): Result<UserResponse> {
        return userApi.updateProfile(sessionId, username, bio, dateOfBirth, newPicture)
    }

    suspend fun getUserFeed(sessionId: String?, maxPostId: Int): Result<List<PostWithAuthor>>{
        return  postApi.getUserFeed(sessionId, maxPostId)
    }

    suspend fun getUserPosts(sessionId: String?, maxPostId: Int, authorId: Int): Result<List<PostWithAuthor>>{
        return  postApi.gestUserPost(sessionId, maxPostId, authorId)
    }

    suspend fun followUser(sessionId: String?, targetUserId: Int): Result<Unit> {
        return followApi.follow(sessionId, targetUserId)  // ← deve restituire Result<Unit>
    }

    suspend fun unfollowUser(sessionId: String?, targetUserId: Int): Result<Unit> {
        return followApi.unfollow(sessionId, targetUserId)  // ← deve restituire Result<Unit>
    }

    fun close() {
        ApiClient.close()
    }
}