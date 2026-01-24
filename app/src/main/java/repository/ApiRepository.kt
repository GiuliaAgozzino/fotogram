package repository

import model.CreateUserResponse
import model.LocationData
import model.Post
import model.User
import repository.api.FollowApi
import repository.api.PostApi
import repository.api.UserApi

class ApiRepository {

    private val userApi = UserApi()
    private val postApi = PostApi()
    private val followApi = FollowApi()

    // ==================== USER ====================

    suspend fun register(username: String, pictureBase64: String): Result<CreateUserResponse> {
        return userApi.register(username, pictureBase64)
    }

    suspend fun getUserInfo(sessionId: String?, userId: Int?): Result<User> {
        return userApi.getUserInfo(sessionId, userId)
    }

    suspend fun updateProfile(
        sessionId: String?,
        username: String,
        bio: String?,
        dateOfBirth: String?,
        newPicture: String?
    ): Result<User> {
        return userApi.updateProfile(sessionId, username, bio, dateOfBirth, newPicture)
    }

    // ==================== FEED ====================


    suspend fun getFeedPostIds(sessionId: String?, maxPostId: Int): Result<List<Int>> {
        return postApi.getFeedPostIds(sessionId, maxPostId)
    }


    suspend fun getUserPostIds(sessionId: String?, authorId: Int, maxPostId: Int): Result<List<Int>> {
        return postApi.getUserPostIds(sessionId, authorId, maxPostId)
    }

    // ==================== POST ====================


    suspend fun getPost(sessionId: String?, postId: Int): Result<Post> {
        return postApi.getPost(sessionId, postId)
    }

    suspend fun newPost(
        sessionId: String?,
        contentText: String,
        contentPicture: String,
        location: LocationData? = null
    ): Result<Post> {
        return postApi.newPost(sessionId, contentText, contentPicture, location)
    }

    // ==================== FOLLOW ====================

    suspend fun followUser(sessionId: String?, targetUserId: Int): Result<Unit> {
        return followApi.follow(sessionId, targetUserId)
    }

    suspend fun unfollowUser(sessionId: String?, targetUserId: Int): Result<Unit> {
        return followApi.unfollow(sessionId, targetUserId)
    }

    // ==================== LIFECYCLE ====================

    fun close() {
        ApiClient.close()
    }
}
