package repository.api

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.LocationResponse
import model.NewPostRequest
import model.PostResponse
import model.PostWithAuthor
import model.UserResponse
import repository.ApiClient

class PostApi {

    private val client = ApiClient.httpClient
    private val baseUrl = ApiClient.BASE_URL
    private val userApi = UserApi()

    private suspend fun getPostInfo(sessionId: String?, postId: Int): Result<PostResponse> {
        return try {
            Log.d("PostApi", "Caricamento post: postId=$postId")

            val response: HttpResponse = client.get("$baseUrl/post/$postId") {
                contentType(ContentType.Application.Json)
                header("x-session-id", sessionId)
            }

            if (response.status.value == 200) {
                val body: PostResponse = response.body()
                Log.d("PostApi", "Post caricato: ${body.id}")
                Result.success(body)
            } else {
                Log.e("PostApi", "Errore caricamento post: ${response.status.value}")
                Result.failure(Exception("Errore caricamento post: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("PostApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun getPostWithAuthor(sessionId: String?, postId: Int): Result<PostWithAuthor> {
        // Step 1: Prendi il post
        val postResult = getPostInfo(sessionId, postId)
        if (postResult.isFailure) {
            return Result.failure(postResult.exceptionOrNull()!!)
        }

        val post = postResult.getOrNull()!!

        // Step 2: Prendi le info dell'autore
        val userResult = userApi.getUserInfo(sessionId, post.authorId)
        if (userResult.isFailure) {
            return Result.failure(userResult.exceptionOrNull()!!)
        }

        val user = userResult.getOrNull()!!

        // Step 3: Combina i dati
        val postWithAuthor = PostWithAuthor(
            postId = post.id,
            authorId = post.authorId,
            authorName = user.username?: "utente sconosciuto",
            authorPicture = user.profilePicture?: "",
            isFollowing = user.isYourFollowing,
            contentPicture = post.contentPicture?: "",
            contentText = post.contentText?: "",
            hasLocation = post.location != null
        )


        return Result.success(postWithAuthor)
    }

    private suspend fun getFeedPostIds(
        sessionId: String?,
        maxPostId: Int,
    ): Result<List<Int>> {

        return try {
            Log.d("PostApi", "Caricamento feed: maxPostId=$maxPostId")

            val response: HttpResponse = client.get("$baseUrl/feed") {
                header("x-session-id", sessionId)
                if(maxPostId != 0){
                    parameter("maxPostId", maxPostId)
                }
            }

            if (response.status.value == 200) {
                val body: List<Int> = response.body()
                Log.d("PostApi", "Feed ricevuto: ${body.size} post IDs")
                Result.success(body)
            } else {
                Log.e("PostApi", "Errore caricamento feed: ${response.status.value}")
                Result.failure(Exception("Errore caricamento feed: ${response.status.value}"))
            }

        } catch (e: Exception) {
            Log.e("PostApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getUserFeed(sessionId: String?, maxPostId: Int): Result<List<PostWithAuthor>> {
        val feedPostIdsResult = getFeedPostIds(sessionId, maxPostId)
        if (feedPostIdsResult.isFailure) {
            return Result.failure(feedPostIdsResult.exceptionOrNull()!!)
        }

        val postIds = feedPostIdsResult.getOrNull()!!
        val posts = mutableListOf<PostWithAuthor>()

        for (id in postIds) {
            val postResult = getPostWithAuthor(sessionId, id)
            if (postResult.isSuccess) {
                posts.add(postResult.getOrNull()!!)
            } else {
                Log.e("PostApi", "Errore caricamento post $id: ${postResult.exceptionOrNull()?.message}")
            }
        }

        return Result.success(posts)
    }
    private suspend fun getUserPostIds(
        sessionId: String?,
        maxPostId: Int,
        authorId: Int
    ): Result<List<Int>> {

        return try {
            Log.d("PostApi", "Caricamento Post: maxPostId=$maxPostId, authorIs=$authorId, sesssionId=$sessionId")

            val response: HttpResponse = client.get("$baseUrl/post/list/$authorId") {
                header("x-session-id", sessionId)
                if(maxPostId != 0){
                    parameter("maxPostId", maxPostId)
                }
            }

            if (response.status.value == 200) {
                val body: List<Int> = response.body()
                Log.d("PostApi", "Post ricevuto: ${body.size} post IDs")
                Result.success(body)
            } else {
                Log.e("PostApi", "Errore caricamento post: ${response.status.value}")
                Result.failure(Exception("Errore caricamento post: ${response.status.value}"))
            }

        } catch (e: Exception) {
            Log.e("PostApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun gestUserPost(
        sessionId: String?,
        maxPostId: Int,
        authorId: Int
    ): Result<List<PostWithAuthor>> {
        val userPostIdsResult = getUserPostIds(sessionId, maxPostId, authorId)
        if (userPostIdsResult.isFailure) {
            return Result.failure(userPostIdsResult.exceptionOrNull()!!)
        }

        val postIds = userPostIdsResult.getOrNull()!!
        val posts = mutableListOf<PostWithAuthor>()

        for (id in postIds) {
            val postResult = getPostWithAuthor(sessionId, id)
            if (postResult.isSuccess) {
                posts.add(postResult.getOrNull()!!)
            } else {
                Log.e("PostApi", "Errore caricamento post $id: ${postResult.exceptionOrNull()?.message}")
            }
        }

        return Result.success(posts)
    }

    suspend fun newPost(
        sessionId: String?,
        contentText: String,
        contentPicture: String,
        location: LocationResponse? = null
    ): Result<PostResponse> {
        return try {
            Log.d("PostApi", "Caricamento nuovo post")

            val response: HttpResponse = client.post("$baseUrl/post") {
                contentType(ContentType.Application.Json)
                header("x-session-id", sessionId)
                setBody(NewPostRequest(contentText, contentPicture, location))
            }

            if (response.status.value == 200) {
                val body: PostResponse = response.body()
                Log.d("PostApi", "Post caricato: ${body.id}")
                Result.success(body)
            } else {
                Log.e("PostApi", "Errore caricamento post: ${response.status.value}")
                Result.failure(Exception("Errore caricamento post: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("PostApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }


    companion object
}