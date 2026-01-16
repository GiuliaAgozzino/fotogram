package repository.api

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import repository.ApiClient

class FollowApi {
    private val client = ApiClient.httpClient
    private val baseUrl = ApiClient.BASE_URL

    suspend fun follow(
        sessionId: String?,
         targetId: Int
    ): Result<Unit> {

        return try {
            Log.d("FollowApi", "Chiamata per follow")

            val response: HttpResponse = client.put("$baseUrl/follow/$targetId") {
                header("x-session-id", sessionId)
            }

            if (response.status.value == 204) {

                Log.d("FollowApi", "Follow riuscito")
                Result.success(Unit)
            } else {
                Log.e("FollowApi", "Errore caricamento nella richiesta}")
                Result.failure(Exception("Errore caricamento nella richiesta: ${response.status.value}"))
            }

        } catch (e: Exception) {
            Log.e("FollowApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun unfollow(
        sessionId: String?,
        targetId: Int
    ): Result<Unit> {

        return try {
            Log.d("FollowApi", "Chiamata per unfollow")

            val response: HttpResponse = client.delete("$baseUrl/follow/$targetId") {
                header("x-session-id", sessionId)
            }

            if (response.status.value == 204) {

                Log.d("FollowApi", "Unfollow riuscito")
                Result.success(Unit)
            } else {
                Log.e("FollowApi", "Errore caricamento nella richiesta}")
                Result.failure(Exception("Errore caricamento nella richiesta: ${response.status.value}"))
            }

        } catch (e: Exception) {
            Log.e("FollowApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }





}