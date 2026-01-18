package view.common

import androidx.compose.runtime.*

@Composable
fun CommonDialogs(
    showError: Boolean,
    onDismissError: () -> Unit,
    onRetry: () -> Unit,
    fullscreenImage: String?,
    onDismissFullscreen: () -> Unit
) {
    // Dialog errore
    if (showError) {
        ErrorDialog(
            onDismiss = onDismissError,
            onRetry = onRetry
        )
    }

    // Dialog immagine fullscreen
    fullscreenImage?.let { image ->
        FullscreenImageDialog(
            imageBase64 = image,
            onDismiss = onDismissFullscreen
        )
    }
}


