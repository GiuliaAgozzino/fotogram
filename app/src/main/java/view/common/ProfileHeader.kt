package view.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.User

@Composable
fun ProfileHeader(
    user: User,
    showEditButton: Boolean = false,
    showFollowButton: Boolean = false,
    isFollowing: Boolean = false,
    isFollowLoading: Boolean = false,
    onEditClick: (() -> Unit)? = null,
    onFollowClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ProfileImage(base64 = user.profilePicture, size = 120.dp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.username ?: "utente sconosciuto",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (user.bio.isNullOrBlank()) "Nessuna bio" else user.bio,
            style = MaterialTheme.typography.bodyMedium,
            color = if (user.bio.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (user.dateOfBirth.isNullOrBlank()) "Data di nascita non impostata"
            else "Nato il: ${user.dateOfBirth}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatItem(label = "Post", value = user.postsCount)
            StatItem(label = "Follower", value = user.followersCount)
            StatItem(label = "Following", value = user.followingCount)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottone Edit o Follow
        when {
            showEditButton && onEditClick != null -> {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modifica Profilo")
                }
            }
            showFollowButton && onFollowClick != null -> {
                if (isFollowing) {
                    OutlinedButton(
                        onClick = onFollowClick,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        enabled = !isFollowLoading
                    ) {
                        if (isFollowLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Non seguire più")
                    }
                } else {
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        enabled = !isFollowLoading
                    ) {
                        if (isFollowLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Segui")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Post di ${user.username ?: "utente sconosciuto"}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}
