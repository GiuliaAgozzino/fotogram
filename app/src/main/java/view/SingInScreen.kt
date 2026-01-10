package view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import viewModel.AuthViewModel
import android.graphics.BitmapFactory
import android.util.Base64
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var pictureBase64 by remember { mutableStateOf("") }

    // Launcher per selezionare l'immagine
    val imagePickerLauncher = rememberImagePickerLauncher { base64 ->
        pictureBase64 = base64
        viewModel.clearError() // Pulisce l'errore quando l'utente seleziona un'immagine
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Benvenuto in Fotogram",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Anteprima immagine profilo
        if (pictureBase64.isNotEmpty()) {
            val imageBytes = Base64.decode(pictureBase64, Base64.NO_WRAP)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto profilo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Nome
        OutlinedTextField(
            value = name,
            onValueChange = {
                if (it.length <= 15) {
                    name = it
                    viewModel.clearError()
                }
            },
            label = { Text("Nome utente") },
            supportingText = { Text("${name.length}/15") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Gray
            )
        )
        

        Spacer(modifier = Modifier.height(16.dp))

        // Bottone selezione immagine
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading
        ) {
            Text(if (pictureBase64.isEmpty()) "Scegli foto profilo" else "Cambia foto profilo")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Messaggio di errore dal ViewModel
        viewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bottone registrazione
        Button(
            onClick = {
                viewModel.register(
                    userName = name,
                    pictureBase64 = pictureBase64,
                    onSuccess = onRegistrationSuccess
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading && name.isNotEmpty() && pictureBase64.isNotEmpty()
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Registrati")
            }
        }
    }
}

/**
 * Composable che gestisce la selezione di un'immagine dalla galleria
 * e la converte in Base64
 */
@Composable
fun rememberImagePickerLauncher(
    onImageSelected: (String) -> Unit
): androidx.activity.result.ActivityResultLauncher<String> {
    val context = LocalContext.current

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                onImageSelected(base64)
            }
        }
    }
}

/**
 * Converte un'immagine URI in Base64, rispettando il limite di 80K caratteri
 */
private fun uriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Comprimi l'immagine fino a rispettare il limite di 80K caratteri
        compressBitmapToBase64(bitmap, maxSizeChars = 80_000)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Comprime un Bitmap in Base64 rispettando il limite di caratteri specificato
 */
private fun compressBitmapToBase64(bitmap: Bitmap, maxSizeChars: Int): String {
    var quality = 90
    var base64String: String

    do {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        quality -= 5
    } while (base64String.length > maxSizeChars && quality > 10)

    return base64String
}