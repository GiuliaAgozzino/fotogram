package view.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

@Composable
fun rememberImagePicker(
    onImageSelected: (String) -> Unit
): ActivityResultLauncher<String> {
    val context = LocalContext.current

    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uriToBase64(context, it)?.let(onImageSelected)
        }
    }
}

private fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            compressBitmapToBase64(bitmap, maxSizeChars = 80_000)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun compressBitmapToBase64(bitmap: Bitmap, maxSizeChars: Int): String {
    var quality = 90
    var base64String: String

    do {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        quality -= 5
    } while (base64String.length > maxSizeChars && quality > 10)

    return base64String
}