package repository

import model.CreateUserResponse
import model.PostWithAuthor
import model.UserResponse
import repository.api.PostApi
import repository.api.UserApi

class ApiRepository {

    private val userApi = UserApi()
    private val postApi = PostApi()

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

    // Post
    suspend fun getPostWithAuthor(sessionId: String?, postId: Int): Result<PostWithAuthor> {
        return postApi.getPostWithAuthor(sessionId, postId)
    }
    suspend fun getUserFeed(sessionId: String?, maxPostId: Int): Result<List<PostWithAuthor>>{
        return  postApi.getUserFeed(sessionId, maxPostId)
    }

    fun close() {
        ApiClient.close()
    }
}