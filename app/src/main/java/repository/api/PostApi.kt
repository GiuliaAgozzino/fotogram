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
import model.LocationData
import model.NewPostRequest
import model.Post
import repository.ApiClient

class PostApi {

    private val client = ApiClient.httpClient
    private val baseUrl = ApiClient.BASE_URL

    /**
     * Ottiene la lista degli ID dei post per il feed.
     */
    suspend fun getFeedPostIds(sessionId: String?, maxPostId: Int): Result<List<Int>> {
        return try {
            Log.d("PostApi", "Caricamento feed: maxPostId=$maxPostId")

            val response: HttpResponse = client.get("$baseUrl/feed") {
                header("x-session-id", sessionId)
                if (maxPostId != 0) {
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

    /**
     * Ottiene la lista degli ID dei post di un utente specifico.
     */
    suspend fun getUserPostIds(sessionId: String?, authorId: Int, maxPostId: Int): Result<List<Int>> {
        return try {
            Log.d("PostApi", "Caricamento post utente: authorId=$authorId, maxPostId=$maxPostId")

            val response: HttpResponse = client.get("$baseUrl/post/list/$authorId") {
                header("x-session-id", sessionId)
                if (maxPostId != 0) {
                    parameter("maxPostId", maxPostId)
                }
            }

            if (response.status.value == 200) {
                val body: List<Int> = response.body()
                Log.d("PostApi", "Post utente ricevuti: ${body.size} post IDs")
                Result.success(body)
            } else {
                Log.e("PostApi", "Errore caricamento post utente: ${response.status.value}")
                Result.failure(Exception("Errore caricamento post utente: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("PostApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Ottiene i dati di un singolo post.
     */
    suspend fun getPost(sessionId: String?, postId: Int): Result<Post> {
        return try {
            Log.d("PostApi", "Caricamento post: postId=$postId")

            val response: HttpResponse = client.get("$baseUrl/post/$postId") {
                contentType(ContentType.Application.Json)
                header("x-session-id", sessionId)
            }

            if (response.status.value == 200) {
                val body: Post = response.body()
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

    /**
     * Crea un nuovo post.
     */
    suspend fun newPost(
        sessionId: String?,
        contentText: String,
        contentPicture: String,
        location: LocationData? = null
    ): Result<Post> {
        return try {
            Log.d("PostApi", "Creazione nuovo post")

            val response: HttpResponse = client.post("$baseUrl/post") {
                contentType(ContentType.Application.Json)
                header("x-session-id", sessionId)
                setBody(NewPostRequest(contentText, contentPicture, location))
            }

            if (response.status.value == 200) {
                val body: Post = response.body()
                Log.d("PostApi", "Post creato: ${body.id}")
                Result.success(body)
            } else {
                Log.e("PostApi", "Errore creazione post: ${response.status.value}")
                Result.failure(Exception("Errore creazione post: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("PostApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }
}
