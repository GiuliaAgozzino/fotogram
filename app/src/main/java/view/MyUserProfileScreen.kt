package view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import model.UserResponse
import viewModel.MyUserProfileViewModel
import view.common.CommonDialogs
import view.common.InfiniteScrollEffect
import view.common.LoadingIndicator
import view.common.ProfileHeader
import view.common.ProfileImage
import view.common.LimitedTextField
import view.common.postItemsWithFooter
import view.common.rememberImagePicker

import java.text.SimpleDateFormat
import java.util.*

/**
 * Schermata del profilo personale dell'utente.
 * Mostra le info utente, pulsante modifica e lista post.
 */
@Composable
fun MyUserProfileScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: MyUserProfileViewModel,
    onNavigateToMap: (postId: Int) -> Unit = {}
) {
    // Stato per immagine fullscreen
    var fullscreenImage by remember { mutableStateOf<String?>(null) }

    // Stato della lista
    val listState = rememberLazyListState()

    // Gestione infinite scroll
    InfiniteScrollEffect(
        listState = listState,
        hasMore = userProfileViewModel.hasMorePosts,
        onLoadMore = { userProfileViewModel.loadMorePosts() },
        tag = "MyUserProfileScreen"
    )

    // Dialogs (errore + fullscreen image)
    CommonDialogs(
        showError = userProfileViewModel.showError,
        onDismissError = { userProfileViewModel.clearError() },
        onRetry = { userProfileViewModel.refresh() },
        fullscreenImage = fullscreenImage,
        onDismissFullscreen = { fullscreenImage = null }
    )

    // Dialog di modifica profilo
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

    // Contenuto principale
    when {
        // Loading iniziale
        userProfileViewModel.isLoading && userProfileViewModel.userInfo == null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        }
        // Contenuto profilo
        userProfileViewModel.userInfo != null -> {
            LazyColumn(
                state = listState,
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header del profilo
                item(key = "profile_header") {
                    ProfileHeader(
                        user = userProfileViewModel.userInfo!!,
                        showEditButton = true,
                        showFollowButton = false,
                        onEditClick = { userProfileViewModel.openEditDialog() }
                    )
                }

                // Post dell'utente con footer
                postItemsWithFooter(
                    posts = userProfileViewModel.userPosts,
                    currentUserId = userProfileViewModel.userInfo?.id, // È il proprio profilo
                    isAuthorClickable = false, // Siamo già sul profilo
                    isLoading = userProfileViewModel.isLoadingPosts,
                    hasMore = userProfileViewModel.hasMorePosts,
                    onAuthorClick = { }, // Nessuna azione
                    onLocationClick = onNavigateToMap,
                    onImageClick = { fullscreenImage = it }
                )
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
    var name by remember { mutableStateOf(currentUser.username ?: "utente sconosciuto") }
    var bio by remember { mutableStateOf(currentUser.bio ?: "") }
    var dateOfBirth by remember { mutableStateOf(currentUser.dateOfBirth ?: "") }
    var newPictureBase64 by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val imagePicker = rememberImagePicker { newPictureBase64 = it }

    // DatePicker dialog
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
                // Foto profilo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileImage(
                        base64 = newPictureBase64 ?: currentUser.profilePicture,
                        size = 60.dp,
                        onClick = { imagePicker.launch("image/*") }
                    )
                    TextButton(onClick = { imagePicker.launch("image/*") }) {
                        Text("Cambia foto")
                    }
                }

                // Campi di testo
                LimitedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome",
                    maxLength = 15
                )

                LimitedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio",
                    maxLength = 100
                )

                // Data di nascita
                DatePickerField(
                    value = dateOfBirth,
                    enabled = !isSaving,
                    onClick = { showDatePicker = true }
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
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Annulla")
            }
        }
    )
}


// ============== COMPONENTI FORM ==============

@Composable
fun DatePickerField(
    value: String,
    enabled: Boolean,
    onClick: () -> Unit
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
            IconButton(onClick = onClick, enabled = enabled) {
                Icon(Icons.Default.DateRange, contentDescription = "Seleziona data")
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
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    ) {
        DatePicker(state = datePickerState, showModeToggle = false)
    }
}


// ============== HELPER FUNCTIONS ==============

private fun parseDataToMillis(dateString: String): Long? {
    if (dateString.isEmpty()) return null
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)?.time
    } catch (e: Exception) {
        null
    }
}

private fun millisToDateString(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))