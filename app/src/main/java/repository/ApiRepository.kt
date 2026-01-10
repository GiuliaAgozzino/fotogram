package repository

import android.util.Log

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import model.CreateUserResponse
import model.UpdateImageRequest
import model.UpdateUserRequest
import model.UserResponse
import io.ktor.client.request.header
import io.ktor.client.request.put


class ApiRepository {

    // Base URL del server
    private val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2526"

    // Creazione dell'HTTP Client (come slide 15 del prof)
    private val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true // Gestisce i null nei campi con default
                    }
                )
            }
        }
    }

    /**
     * STEP 1: Crea un nuovo utente
     * POST /user (senza body)
     * Ritorna: userId e sessionId
     */
    private suspend fun createUser(): Result<CreateUserResponse> {
        return try {
            val urlString = "$baseUrl/user"

            Log.d("ApiRepository", "Step 1: Creating user...")

            val response: HttpResponse = httpClient.post(urlString) {
                contentType(ContentType.Application.Json)
            }

            if (response.status.value == 200) {
                val body: CreateUserResponse = response.body()
                Log.d("ApiRepository", "User created! userId=${body.userId}, sessionId=${body.sessionId}")
                Result.success(body)
            } else {
                Log.e("ApiRepository", "Error creating user: ${response.status.value}")
                Result.failure(Exception("Error creating user: ${response.status.value}"))
            }

        } catch (e: Exception) {
            Log.e("ApiRepository", "Network error creating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * STEP 2: Aggiorna le informazioni dell'utente (username, bio, dateOfBirth)
     * PUT /user
     * Richiede: sessionId nell'header "x-session-id"
     */
    private suspend fun updateUserInfo(
        sessionId: String,
        username: String
    ): Result<UserResponse> {
        return try {
            val urlString = "$baseUrl/user"

            Log.d("ApiRepository", "Step 2: Updating user info...")

            val requestBody = UpdateUserRequest(
                username = username,
                bio = null,
                dateOfBirth = null
            )

            val response: HttpResponse = httpClient.put(urlString) {
                contentType(ContentType.Application.Json)
                header("x-session-id", sessionId)
                setBody(requestBody)
            }

            if (response.status.value == 200) {
                val body: UserResponse = response.body()
                Log.d("ApiRepository", "User info updated!")
                Result.success(body)
            } else {
                Log.e("ApiRepository", "Error updating user info: ${response.status.value}")
                Result.failure(Exception("Error updating user info: ${response.status.value}"))
            }

        } catch (e: Exception) {
            Log.e("ApiRepository", "Network error updating user info: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * STEP 3: Aggiorna l'immagine profilo
     * PUT /user/image
     * Richiede: sessionId nell'header "x-session-id"
     */
    private suspend fun updateUserImage(
        sessionId: String,
        base64Image: String
    ): Result<UserResponse> {
        return try {
            val urlString = "$baseUrl/user/image"

            Log.d("ApiRepository", "Step 3: Updating profile image...")

            val requestBody = UpdateImageRequest(
                base64 = base64Image
            )

            val response: HttpResponse = httpClient.put(urlString) {
                contentType(ContentType.Application.Json)
                header("x-session-id", sessionId)
                setBody(requestBody)
            }

            if (response.status.value == 200) {
                val body: UserResponse = response.body()
                Log.d("ApiRepository", "Profile image updated!")
                Result.success(body)
            } else {
                Log.e("ApiRepository", "Error updating image: ${response.status.value}")
                Result.failure(Exception("Error updating image: ${response.status.value}"))
            }

        } catch (e: Exception) {
            Log.e("ApiRepository", "Network error updating image: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * REGISTRAZIONE COMPLETA (3 step in sequenza)
     * 1. Crea utente → ottieni userId e sessionId
     * 2. Aggiorna username
     * 3. Aggiorna immagine profilo
     */
    suspend fun register(username: String, pictureBase64: String): Result<CreateUserResponse> {
        // Step 1: Crea utente
        val createResult = createUser()
        if (createResult.isFailure) {
            return createResult
        }

        val userInfo = createResult.getOrNull()!!
        val sessionId = userInfo.sessionId
        val userId = userInfo.userId

        // Step 2: Aggiorna username
        val updateInfoResult = updateUserInfo(sessionId, username)
        if (updateInfoResult.isFailure) {
            Log.e("ApiRepository", "Failed to update username, but user was created")
            // Anche se fallisce, abbiamo comunque userId e sessionId
        }

        // Step 3: Aggiorna immagine
        val updateImageResult = updateUserImage(sessionId, pictureBase64)
        if (updateImageResult.isFailure) {
            Log.e("ApiRepository", "Failed to update image, but user was created")
            // Anche se fallisce, abbiamo comunque userId e sessionId
        }

        Log.d("ApiRepository", "Registration completed! userId=$userId")
        return Result.success(userInfo)
    }

    /**
     * Chiude l'HTTP client quando non serve più
     */
    fun close() {
        httpClient.close()
    }
}