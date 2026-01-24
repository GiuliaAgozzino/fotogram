package repository

import android.content.Context
import android.util.Log
import model.CreateUserResponse
import model.LocationData
import model.Post
import model.PostDao
import model.User
import model.UserDao
import model.database.DatabaseBuilder
import model.toEntity
import model.toPost
import repository.api.FollowApi
import repository.api.PostApi
import repository.api.UserApi
import model.toUser

class ApiRepository(context: Context) {

    private val userApi = UserApi()
    private val postApi = PostApi()
    private val followApi = FollowApi()

    // Database Room
    private val db = DatabaseBuilder.getInstance(context)
    private val postDao: PostDao = db.postDao()
    private val userDao: UserDao = db.userDao()

    // ==================== USER ====================

    suspend fun register(username: String, pictureBase64: String): Result<CreateUserResponse> {
        return userApi.register(username, pictureBase64)
    }

    suspend fun getUserInfo(sessionId: String?, userId: Int?): Result<User> {
        if (userId == null) {
            return Result.failure(Exception("userId è null"))
        }

        //  Controllo cache locale
        val cachedUser = userDao.getUserById(userId)
        if (cachedUser != null) {
            Log.d("ApiRepository", "User $userId trovato in cache")
            return Result.success(cachedUser.toUser())
        }

        //Non in cache
        Log.d("ApiRepository", "User $userId non in cache, scarico da rete")
        val result = userApi.getUserInfo(sessionId, userId)

        // salvo in cache
        if (result.isSuccess) {
            val user = result.getOrNull()!!
            userDao.insertUser(user.toEntity())
            Log.d("ApiRepository", "User $userId salvato in cache")
        }

        return result
    }


    suspend fun refreshUserInfo(sessionId: String?, userId: Int): Result<User> {
        val result = userApi.getUserInfo(sessionId, userId)

        if (result.isSuccess) {
            val user = result.getOrNull()!!
            userDao.insertUser(user.toEntity())
            Log.d("ApiRepository", "User $userId refreshato e salvato in cache")
        }

        return result
    }

    suspend fun updateProfile(
        sessionId: String?,
        username: String,
        bio: String?,
        dateOfBirth: String?,
        newPicture: String?
    ): Result<User> {
        val result = userApi.updateProfile(sessionId, username, bio, dateOfBirth, newPicture)

        // Aggiorna cache se successo
        if (result.isSuccess) {
            val user = result.getOrNull()!!
            userDao.insertUser(user.toEntity())
        }

        return result
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
        // Controllo cache locale
        val cachedPost = postDao.getPostById(postId)
        if (cachedPost != null) {
            Log.d("ApiRepository", "Post $postId trovato in cache")
            return Result.success(cachedPost.toPost())
        }

        //Non in cache
        Log.d("ApiRepository", "Post $postId non in cache, scarico da rete")
        val result = postApi.getPost(sessionId, postId)

        // salvo in cache
        if (result.isSuccess) {
            val post = result.getOrNull()!!
            postDao.insertPost(post.toEntity())
            Log.d("ApiRepository", "Post $postId salvato in cache")
        }

        return result
    }

    suspend fun newPost(
        sessionId: String?,
        contentText: String,
        contentPicture: String,
        location: LocationData? = null
    ): Result<Post> {
        val result = postApi.newPost(sessionId, contentText, contentPicture, location)

        // Salva in cache se successo
        if (result.isSuccess) {
            val post = result.getOrNull()!!
            postDao.insertPost(post.toEntity())
            Log.d("ApiRepository", "Nuovo post ${post.id} salvato in cache")
        }

        return result
    }

    // ==================== FOLLOW ====================

    suspend fun followUser(sessionId: String?, targetUserId: Int): Result<Unit> {
        val result = followApi.follow(sessionId, targetUserId)

        // Refresh cache utente dopo follow
        if (result.isSuccess) {
            refreshUserInfo(sessionId, targetUserId)
        }

        return result
    }

    suspend fun unfollowUser(sessionId: String?, targetUserId: Int): Result<Unit> {
        val result = followApi.unfollow(sessionId, targetUserId)

        // Refresh cache utente dopo unfollow
        if (result.isSuccess) {
            refreshUserInfo(sessionId, targetUserId)
        }

        return result
    }

    // ==================== CACHE MANAGEMENT ====================


    suspend fun clearCache() {
        postDao.clearPost()
        userDao.clearUser()
        Log.d("ApiRepository", "Cache pulita")
    }

    // ==================== LIFECYCLE ====================

    fun close() {
        ApiClient.close()
    }
}
