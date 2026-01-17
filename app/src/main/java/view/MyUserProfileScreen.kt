package view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import view.common.ErrorDialog
import view.common.FullscreenImageDialog
import view.common.rememberImagePicker
import view.common.LoadingIndicator
import view.common.ProfileImage
import view.common.LimitedTextField
import view.common.ProfileHeader
import view.common.PostItem

import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyUserProfileScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: MyUserProfileViewModel,
) {
    var fullscreenImage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // Infinite scroll
    val lastVisibleIndex = remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
    }

    LaunchedEffect(lastVisibleIndex.value) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val total = listState.layoutInfo.totalItemsCount
        val threshold = total - 3

        if (lastVisible != null && lastVisible >= threshold && userProfileViewModel.hasMorePosts) {
            Log.d("MyUserProfileScreen", "Trigger loadMore - lastVisible=$lastVisible, total=$total")
            userProfileViewModel.loadMorePosts()
        }
    }

    // Dialog errore
    if (userProfileViewModel.showError) {
        ErrorDialog(
            onDismiss = { userProfileViewModel.clearError() },
            onRetry = { userProfileViewModel.refresh() }
        )
    }

    // Dialog immagine fullscreen
    fullscreenImage?.let { image ->
        FullscreenImageDialog(
            imageBase64 = image,
            onDismiss = { fullscreenImage = null }
        )
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

    // Contenuto principale
    when {
        userProfileViewModel.isLoading && userProfileViewModel.userInfo == null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        }

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

                // Post dell'utente
                items(
                    items = userProfileViewModel.userPosts,
                    key = { post -> post.postId }
                ) { post ->
                    PostItem(
                        post = post,
                        isOwnPost = true,
                        isAuthorClickable = false,
                        onAuthorClick = { },
                        onLocationClick = { /* TODO: navigate to map */ },
                        onImageClick = { imageBase64 ->
                            fullscreenImage = imageBase64
                        }
                    )
                }

                // Footer: loading o fine post
                item(key = "footer") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            userProfileViewModel.isLoadingPosts -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            !userProfileViewModel.hasMorePosts && userProfileViewModel.userPosts.isNotEmpty() -> {
                                Text(
                                    text = "Hai visto tutti i post 🎉",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            !userProfileViewModel.hasMorePosts && userProfileViewModel.userPosts.isEmpty() -> {
                                Text(
                                    text = "Nessun post pubblicato",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// DIALOG MODIFICA PROFILO

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

                LimitedTextField(value = name, onValueChange = { name = it }, label = "Nome", maxLength = 15)
                LimitedTextField(value = bio, onValueChange = { bio = it }, label = "Bio", maxLength = 100)

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
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Annulla") }
        }
    )
}

// COMPONENTI FORM

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

private fun parseDataToMillis(dateString: String): Long? {
    if (dateString.isEmpty()) return null
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)?.time
    } catch (e: Exception) { null }
}

private fun millisToDateString(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))