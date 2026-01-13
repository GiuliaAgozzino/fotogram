package view.common

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.Dp

@Composable
fun ProfileImage(
    base64: String?,
    size: Dp,
    onClick: (() -> Unit)? = null
) {
    if (base64.isNullOrEmpty()) return

    val imageBytes = Base64.decode(base64, Base64.NO_WRAP)
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Foto profilo",
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentScale = ContentScale.Crop
    )
}