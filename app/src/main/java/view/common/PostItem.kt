package view.common

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.Post
import model.User


@Composable
fun PostItem(
    post: Post,
    author: User,
    isOwnPost: Boolean = false,
    isAuthorClickable: Boolean = true,
    onAuthorClick: (authorId: Int) -> Unit,
    onImageClick: (imageBase64: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Bordo colorato per i post degli utenti seguiti
    val borderModifier = if (author.isYourFollowing && !isOwnPost) {
        Modifier.border(
            width = 3.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = CardDefaults.shape
        )
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(borderModifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header con autore
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isAuthorClickable) {
                            Modifier.clickable { onAuthorClick(post.authorId) }
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileImage(
                    base64 = author.profilePicture,
                    size = 40.dp
                )

                Text(
                    text = author.username ?: "utente sconosciuto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Immagine del post
            PostImage(
                base64 = post.contentPicture,
                onClick = { onImageClick(post.contentPicture) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Testo del post
                if (!post.contentText.isNullOrBlank()) {
                    Text(
                        text = post.contentText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Posizione
                if (post.location?.latitude != null && post.location.longitude != null) {
                    Box(modifier = Modifier.align(Alignment.End)) {
                        PostLocationButton(post = post)
                    }
                }
            }
        }
    }
}

@Composable
fun PostImage(
    base64: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(base64) {
        if (base64.isNullOrBlank()) {
            null
        } else {
            try {
                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = bitmap != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Immagine post",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = "Immagine non disponibile",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Immagine non disponibile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

