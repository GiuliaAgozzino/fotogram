package view.common

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.PostWithAuthor
import androidx.compose.foundation.border

@Composable
fun PostItem(
    post: PostWithAuthor,
    isOwnPost: Boolean = false,
    isAuthorClickable: Boolean = true,
    onAuthorClick: (authorId: Int) -> Unit,
    onLocationClick: (postId: Int) -> Unit,
    onImageClick: (imageBase64: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Bordo solo per amici (più spesso)
    val borderModifier = if (post.isFollowing && !isOwnPost) {
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
            // Header con autore - MODIFICATO
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
                    base64 = post.authorPicture,
                    size = 40.dp
                )

                Text(
                    text = post.authorName ?: "utente sconosciuto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }


            Spacer(modifier = Modifier.height(12.dp))

            // Immagine del post (o placeholder se non valida)
            PostImage(
                base64 = post.contentPicture,
                onClick = { post.contentPicture?.let { onImageClick(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: testo sopra, posizione sotto a destra
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

                // Posizione (sotto, allineata a destra)
                if (post.hasLocation) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                            .clickable { onLocationClick(post.postId) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Posizione",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Mappa",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                contentDescription = "Post image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            //  immagine mancante/non valida
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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