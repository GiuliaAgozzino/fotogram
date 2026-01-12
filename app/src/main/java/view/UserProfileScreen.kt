package view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.PersonAdd
//import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import model.UserResponse
import viewModel.UserProfileViewModel
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

// ============== SCHERMATA PROFILO UTENTE ==============



@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: UserProfileViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            userProfileViewModel.isLoading -> LoadingIndicator()
            userProfileViewModel.errorMessage != null -> ErrorMessage(
                message = userProfileViewModel.errorMessage!!,
                onRetry = { userProfileViewModel.refresh() }
            )
            userProfileViewModel.userInfo != null -> {
                ProfileHeader(
                    user = userProfileViewModel.userInfo!!,
                    showEditButton = true,
                    onEditClick = { userProfileViewModel.openEditDialog() }
                )
            }
            else -> Text("Nessun dato disponibile")
        }
    }

    // Dialog di modifica
    if (userProfileViewModel.showEditDialog && userProfileViewModel.userInfo != null) {
        EditProfileDialog(
            currentUser = userProfileViewModel.userInfo!!,
            isSaving = userProfileViewModel.isSaving,
            onDismiss = { userProfileViewModel.closeEditDialog() },
            onSave = { name, bio, dateOfBirth, picture ->
                userProfileViewModel.updateProfile(name, bio, dateOfBirth, picture)
            }
        )
    }
}

// ============== COMPONENTI RIUTILIZZABILI ==============

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Riprova") }
    }
}

@Composable
fun ProfileImage(base64: String?, size: Dp, onClick: (() -> Unit)? = null) {
    val imageBytes = Base64.decode(base64, Base64.NO_WRAP)
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Foto profilo",
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    )
}

@Composable
fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

// ============== PROFILE HEADER (RIUTILIZZABILE) ==============

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
        // Immagine profilo
        ProfileImage(base64 = user.profilePicture, size = 120.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // Nome
        Text(text = user.username, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(8.dp))

        // Bio
        Text(
            text = if (user.bio.isNullOrBlank()) "Nessuna bio" else user.bio,
            style = MaterialTheme.typography.bodyMedium,
            color = if (user.bio.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Data di nascita
        Text(
            text = if (user.dateOfBirth.isNullOrBlank()) "Data di nascita non impostata"
            else "Nato il: ${user.dateOfBirth}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Statistiche
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatItem(label = "Post", value = user.postCount)
            StatItem(label = "Follower", value = user.followersCount)
            StatItem(label = "Following", value = user.followingCount)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottone - Modifica OPPURE Segui/Non seguire
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
                        //Icon(Icons.Default.PersonRemove, null, Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Non seguire")
                    }
                } else {
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        //Icon(Icons.Default.PersonAdd, null, Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Segui")
                    }
                }
            }
        }
    }
}

// ============== DIALOG MODIFICA PROFILO ==============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentUser: UserResponse,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, bio: String, dateOfBirth: String, picture: String?) -> Unit
) {
    var name by remember { mutableStateOf(currentUser.username) }
    var bio by remember { mutableStateOf(currentUser.bio ?: "") }
    var dateOfBirth by remember { mutableStateOf(currentUser.dateOfBirth ?: "") }
    var newPictureBase64 by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberImagePicker(context) { newPictureBase64 = it }

    // DatePicker
    if (showDatePicker) {
        DatePickerDialogComponent(
            initialDate = dateOfBirth,
            onDateSelected = { dateOfBirth = it },
            onDismiss = { showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Modifica Profilo") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Immagine
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileImage(
                        base64 = newPictureBase64 ?: currentUser.profilePicture,
                        size = 60.dp,
                        onClick = { imagePickerLauncher.launch("image/*") }
                    )
                    TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Cambia foto")
                    }
                }

                // Nome
                LimitedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome",
                    maxLength = 15,
                    enabled = !isSaving
                )

                // Bio
                LimitedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio",
                    maxLength = 100,
                    enabled = !isSaving,
                    singleLine = false,
                    maxLines = 3
                )

                // Data di nascita
                DatePickerField(
                    value = dateOfBirth,
                    enabled = !isSaving,
                    onClick = { showDatePicker = true },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, bio, dateOfBirth, newPictureBase64) },
                enabled = !isSaving && name.isNotEmpty()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Salva")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Annulla") }
        }
    )
}

// ============== COMPONENTI FORM ==============

@Composable
fun LimitedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maxLength: Int,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= maxLength) onValueChange(it) },
        label = { Text(label) },
        supportingText = { Text("${value.length}/$maxLength") },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines
    )
}

@Composable
fun DatePickerField(
    value: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedTextField(
        value = value.ifEmpty { "Seleziona data" },
        onValueChange = { },
        label = { Text("Data di nascita") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        enabled = false,
        readOnly = true,
        singleLine = true,
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Icona calendario
                IconButton(onClick = onClick, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Seleziona data"
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogComponent(
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parseDataToMillis(initialDate)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(millisToDateString(it)) }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    ) {
        DatePicker(state = datePickerState, showModeToggle = false)
    }
}

// ============== HELPER FUNCTIONS ==============

@Composable
fun rememberImagePicker(
    context: android.content.Context,
    onImageSelected: (String) -> Unit
): androidx.activity.result.ActivityResultLauncher<String> {
    return rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uriToBase64(context, uri)?.let { base64 -> onImageSelected(base64) }
        }
    }
}

private fun uriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        compressBitmapToBase64(bitmap, 80_000)
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

private fun parseDataToMillis(dateString: String): Long? {
    if (dateString.isEmpty()) return null
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)?.time
    } catch (e: Exception) { null }
}

private fun millisToDateString(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))