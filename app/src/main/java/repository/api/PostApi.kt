package repository.api

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.PostResponse
import model.PostWithAuthor
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

    suspend fun getPostWithAuthor(sessionId: String?, postId: Int): Result<PostWithAuthor> {
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
            authorName = user.username?: "sconosciuto",
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
        limit: Int = 10
    ): Result<List<Int>> {

        return try {
            Log.d("PostApi", "Caricamento feed: maxPostId=$maxPostId, limit=$limit")

            val response: HttpResponse = client.get("$baseUrl/feed") {
                header("x-session-id", sessionId)
                if(maxPostId != 0){
                    parameter("maxPostId", maxPostId)
                }
                parameter("limit", limit)
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

    companion object
}