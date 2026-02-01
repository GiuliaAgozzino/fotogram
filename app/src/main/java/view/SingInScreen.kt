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
import view.common.ErrorDialog
import view.common.LimitedTextField
import view.common.rememberImagePicker

@Composable
fun SignInScreen(
    viewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var pictureBase64 by remember { mutableStateOf("") }

    val imagePicker = rememberImagePicker { base64 ->
        pictureBase64 = base64
    }

    if (viewModel.showError) {
        ErrorDialog(
            onDismiss = { viewModel.clearError() }
        )
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

        LimitedTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nome utente",
            maxLength = 15,
            enabled = !viewModel.isLoading,
            allowSpaces = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading
        ) {
            Text(if (pictureBase64.isEmpty()) "Scegli foto profilo" else "Cambia foto profilo")
        }

        Spacer(modifier = Modifier.height(32.dp))

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