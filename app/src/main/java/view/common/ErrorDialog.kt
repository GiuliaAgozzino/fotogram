
package view.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    message: String = "Si è verificato un errore. Riprova più tardi."
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Errore") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            if (onRetry != null) {
                TextButton(onClick = {
                    onDismiss()
                    onRetry()
                }) {
                    Text("Riprova")
                }
            }
        }
    )
}