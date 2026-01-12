package repository

import android.util.Log

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
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

    private val baseUrl = "https://develop.ewlab.di.unimi.it/mc/2526"

    private val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                )
            }
        }
    }

    /**
     * STEP 1: Crea un nuovo utente
     */
    private suspend fun createUser(): Result<CreateUserResponse> {
        return try {
            val urlString = "$baseUrl/user"

            Log.d("Auth-ApiRepository", "Step 1: Creating user...")

            val response: HttpResponse = httpClient.post(urlString) {
                contentType(ContentType.Application.Json)
            }

            if (response.status.value == 200) {
                val body: CreateUserResponse = response.body()
                Log.d("Auth-ApiRepository", "User created! userId=${body.userId}, sessionId=${body.sessionId}")
                Result.success(body)
            } else {
                Log.e("Auth-ApiRepository", "Error creating user: ${response.status.value}")
                Result.failure(Exception("Error creating user: ${response.status.value}"))
            }

        } catch (e: Exception) {
            Log.e("Auth-ApiRepository", "Network error creating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * STEP 2: Aggiorna le informazioni dell'utente (username, bio, dateOfBirth)
     * PUT /user
     * Ora accetta anche bio e dateOfBirth
     */
    private suspend fun updateUserInfo(
        sessionId: String,
        username: String,
        bio: String? = null,
        dateOfBirth: String? = null
    ): Result<UserResponse> {
        return try {
            val urlString = "$baseUrl/user"

            Log.d("ApiRepository", "Updating user info...")

            val requestBody = UpdateUserRequest(
                username = username,
                bio = bio,
                dateOfBirth = dateOfBirth
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
     */
    private suspend fun updateUserImage(
        sessionId: String,
        base64Image: String
    ): Result<UserResponse> {
        return try {
            val urlString = "$baseUrl/user/image"

            Log.d("ApiRepository", "Updating profile image...")

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

        // Step 2: Aggiorna username (senza bio e dateOfBirth in registrazione)
        val updateInfoResult = updateUserInfo(sessionId, username)
        if (updateInfoResult.isFailure) {
            Log.e("Auth-ApiRepository", "Failed to update username, but user was created")
        }

        // Step 3: Aggiorna immagine
        val updateImageResult = updateUserImage(sessionId, pictureBase64)
        if (updateImageResult.isFailure) {
            Log.e("Auth-ApiRepository", "Failed to update image, but user was created")
        }

        Log.d("Auth-ApiRepository", "Registration completed! userId=$userId")
        return Result.success(userInfo)
    }

    /**
     * AGGIORNA PROFILO COMPLETO
     * Metodo pubblico che combina updateUserInfo e updateUserImage
     */
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

        return try {
            // Step 1: Aggiorna info utente (nome, bio, data di nascita)
            val userResult = updateUserInfo(
                sessionId = sessionId,
                username = username,
                bio = bio,
                dateOfBirth = dateOfBirth
            )

            if (userResult.isFailure) {
                return userResult
            }

            // Step 2: Se c'è una nuova immagine, aggiornala
            if (newPicture != null) {
                val imageResult = updateUserImage(sessionId, newPicture)
                if (imageResult.isFailure) {
                    return imageResult
                }
                return imageResult
            }

            return userResult

        } catch (e: Exception) {
            Log.e("ApiRepository", "Error updating profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Ottieni info utente
     */
    suspend fun getUserInfo(
        sessionId: String?,
        userId: Int?
    ): Result<UserResponse> {
        return try {
            val urlString = "$baseUrl/user/$userId"

            val response: HttpResponse = httpClient.get(urlString) {
                contentType(ContentType.Application.Json)
                header("x-session-id", sessionId)
            }

            if (response.status.value == 200) {
                val body: UserResponse = response.body()
                Log.d("ApiRepository", "Success retrieving user info: ${response.status.value}")
                Result.success(body)
            } else {
                Log.e("ApiRepository", "Error retrieving user info: ${response.status.value}")
                Result.failure(Exception("Error retrieving user info: ${response.status.value}"))
            }

        } catch (e: Exception) {
            Log.e("ApiRepository", "Network error: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}