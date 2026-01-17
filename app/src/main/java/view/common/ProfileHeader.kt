package view.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.UserResponse

@Composable
fun ProfileHeader(
    user: UserResponse,
    showEditButton: Boolean = false,
    showFollowButton: Boolean = false,
    isFollowing: Boolean = false,
    onEditClick: (() -> Unit)? = null,
    onFollowClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ProfileImage(base64 = user.profilePicture, size = 120.dp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = user.username ?: "sconosciuto", style = MaterialTheme.typography.headlineMedium)

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
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Non seguire")
                    }
                } else {
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Segui")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Post di ${user.username}",
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