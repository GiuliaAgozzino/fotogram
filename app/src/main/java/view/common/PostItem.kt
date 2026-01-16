package view.common

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import model.PostWithAuthor
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun PostItem(
    post: PostWithAuthor,
    isOwnPost: Boolean = false,  // ← NUOVO: true se è un post dell'utente loggato
    onAuthorClick: (authorId: Int) -> Unit,
    onLocationClick: (postId: Int) -> Unit,
    onImageClick: (imageBase64: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determina lo stile del bordo in base al tipo di post
    val borderModifier = when {
        isOwnPost -> Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.tertiary,  // Colore diverso per i propri post
            shape = CardDefaults.shape
        )
        post.isFollowing -> Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,  // Bordo per gli amici
            shape = CardDefaults.shape
        )
        else -> Modifier  // Nessun bordo per gli estranei
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
                    .clickable { onAuthorClick(post.authorId) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileImage(
                    base64 = post.authorPicture,
                    size = 40.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = post.authorName ?: "sconosciuto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Badge per distinguere il tipo di post
                        when {
                            isOwnPost -> {
                                // Badge "Tu" per i propri post
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "Tu",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            post.isFollowing -> {
                                // Badge "Amico" per i post degli amici
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "Amico",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            // Nessun badge per gli estranei
                        }
                    }

                    // Location sotto il nome (se presente)
                    if (post.hasLocation) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable { onLocationClick(post.postId) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Posizione",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Vedi posizione",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Testo del post (se presente) - PRIMA dell'immagine
            post.contentText?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Immagine del post (se presente) - CLICCABILE per fullscreen
            post.contentPicture?.let { picture ->
                PostImage(
                    base64 = picture,
                    onClick = { onImageClick(picture) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}

@Composable
fun PostImage(
    base64: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(base64) {
        try {
            val imageBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Post image",
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() },
            contentScale = ContentScale.Crop
        )
    }
}