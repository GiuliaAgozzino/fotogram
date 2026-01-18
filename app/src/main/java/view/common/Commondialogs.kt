package view.common

import androidx.compose.runtime.*

/**
 * Composable che gestisce i dialogs comuni usati in più screen:
 * - Dialog di errore con retry
 * - Dialog per immagine fullscreen
 *
 * @param showError Se mostrare il dialog di errore
 * @param onDismissError Callback quando si chiude il dialog di errore
 * @param onRetry Callback quando si clicca su Riprova
 * @param fullscreenImage Immagine base64 da mostrare fullscreen (null = nascosto)
 * @param onDismissFullscreen Callback quando si chiude l'immagine fullscreen
 */
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

/**
 * Stato per gestire l'immagine fullscreen in un Composable.
 * Semplifica la gestione dello stato dell'immagine fullscreen.
 */
class FullscreenImageState {
    var currentImage by mutableStateOf<String?>(null)
        private set

    fun show(imageBase64: String) {
        currentImage = imageBase64
    }

    fun dismiss() {
        currentImage = null
    }
}

/**
 * Remember per FullscreenImageState.
 */
@Composable
fun rememberFullscreenImageState(): FullscreenImageState {
    return remember { FullscreenImageState() }
}