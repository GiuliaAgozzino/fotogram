package repository.api

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import model.CreateUserResponse
import model.UpdateImageRequest
import model.UpdateUserRequest
import model.UserResponse
import repository.ApiClient

class UserApi {

    private val client = ApiClient.httpClient
    private val baseUrl = ApiClient.BASE_URL

    // ============== AUTH ==============

    suspend fun register(username: String, pictureBase64: String): Result<CreateUserResponse> {
        // Step 1: Crea utente
        val createResult = createUser()
        if (createResult.isFailure) {
            return createResult
        }

        val userInfo = createResult.getOrNull()!!
        val sessionId = userInfo.sessionId

        // Step 2: Aggiorna username
        val updateInfoResult = updateUserInfo(sessionId, username, null, null)
        if (updateInfoResult.isFailure) {
            Log.e("UserApi", "Username non aggiornato, ma utente creato")
        }

        // Step 3: Aggiorna immagine
        val updateImageResult = updateUserImage(sessionId, pictureBase64)
        if (updateImageResult.isFailure) {
            Log.e("UserApi", "Immagine non aggiornata, ma utente creato")
        }

        Log.d("UserApi", "Registrazione completata: userId=${userInfo.userId}")
        return Result.success(userInfo)
    }

    private suspend fun createUser(): Result<CreateUserResponse> {
        return try {
            Log.d("UserApi", "Creazione utente...")

            val response: HttpResponse = client.post("$baseUrl/user") {
                contentType(ContentType.Application.Json)
            }

            if (response.status.value == 200) {
                val body: CreateUserResponse = response.body()
                Log.d("UserApi", "Utente creato: userId=${body.userId}")
                Result.success(body)
            } else {
                Log.e("UserApi", "Errore creazione utente: ${response.status.value}")
                Result.failure(Exception("Errore creazione utente: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("UserApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============== PROFILO ==============

    suspend fun getUserInfo(sessionId: String?, userId: Int?): Result<UserResponse> {
        return try {
            Log.d("UserApi", "Caricamento profilo: userId=$userId")

            val response: HttpResponse = client.get("$baseUrl/user/$userId") {
                contentType(ContentType.Application.Json)
                header("x-session-id", sessionId)
            }

            if (response.status.value == 200) {
                val body: UserResponse = response.body()
                Log.d("UserApi", "Profilo caricato: ${body.username} post count ${body.postsCount} following count ${body.followingCount}")
                Result.success(body)
            } else {
                Log.e("UserApi", "Errore caricamento profilo: ${response.status.value}")
                Result.failure(Exception("Errore caricamento profilo: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("UserApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        sessionId: String?,
        username: String,
        bio: String?,
        dateOfBirth: String?,
        newPicture: String?
    ): Result<UserResponse> {
        if (sessionId == null) {
            return Result.failure(Exception("Session ID mancante"))
        }

        Log.d("UserApi", "Aggiornamento profilo: username=$username")

        // Step 1: Aggiorna info utente
        val userResult = updateUserInfo(sessionId, username, bio, dateOfBirth)
        if (userResult.isFailure) {
            return userResult
        }

        // Step 2: Se c'è una nuova immagine, aggiornala
        return if (newPicture != null) {
            updateUserImage(sessionId, newPicture)
        } else {
            userResult
        }
    }

    // ============== HELPER PRIVATI ==============

    private suspend fun updateUserInfo(
        sessionId: String,
        username: String,
        bio: String?,
        dateOfBirth: String?
    ): Result<UserResponse> {
        return try {
            val response: HttpResponse = client.put("$baseUrl/user") {
                contentType(ContentType.Application.Json)
                header("x-session-id", sessionId)
                setBody(UpdateUserRequest(username, bio, dateOfBirth))
            }

            if (response.status.value == 200) {
                Log.d("UserApi", "Info utente aggiornate")
                Result.success(response.body())
            } else {
                Log.e("UserApi", "Errore aggiornamento info: ${response.status.value}")
                Result.failure(Exception("Errore aggiornamento info: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("UserApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun updateUserImage(sessionId: String, base64Image: String): Result<UserResponse> {
        return try {
            val response: HttpResponse = client.put("$baseUrl/user/image") {
                contentType(ContentType.Application.Json)
                header("x-session-id", sessionId)
                setBody(UpdateImageRequest(base64 = base64Image))
            }

            if (response.status.value == 200) {
                Log.d("UserApi", "Immagine profilo aggiornata")
                Result.success(response.body())
            } else {
                Log.e("UserApi", "Errore aggiornamento immagine: ${response.status.value}")
                Result.failure(Exception("Errore aggiornamento immagine: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("UserApi", "Errore di rete: ${e.message}", e)
            Result.failure(e)
        }
    }
}