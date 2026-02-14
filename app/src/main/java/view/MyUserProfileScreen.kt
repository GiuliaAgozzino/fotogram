package view

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
import model.User
import view.common.*
import viewModel.MyUserProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyUserProfileScreen(
    modifier: Modifier = Modifier,
    userProfileViewModel: MyUserProfileViewModel
) {
    var fullscreenImage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    InfiniteScrollEffect(
        listState = listState,
        hasMore = userProfileViewModel.hasMorePosts,
        onLoadMore = { userProfileViewModel.loadMorePosts() },
        tag = "MyUserProfileScreen"
    )

    CommonDialogs(
        showError = userProfileViewModel.showError,
        onDismissError = { userProfileViewModel.clearError() },
        onRetry = { userProfileViewModel.refresh() },
        fullscreenImage = fullscreenImage,
        onDismissFullscreen = { fullscreenImage = null }
    )

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

    when {
        userProfileViewModel.isLoading && userProfileViewModel.userInfo == null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                item(key = "profile_header") {
                    ProfileHeader(
                        user = userProfileViewModel.userInfo!!,
                        showEditButton = true,
                        showFollowButton = false,
                        onEditClick = { userProfileViewModel.openEditDialog() }
                    )
                }

                items(
                    items = userProfileViewModel.userPosts,
                    key = { it.id }
                ) { post ->
                    PostItem(
                        post = post,
                        author = userProfileViewModel.userInfo!!,
                        isOwnPost = true,
                        isAuthorClickable = false,
                        onAuthorClick = { },
                        onImageClick = { fullscreenImage = it }
                    )
                }

                item(key = "footer") {
                    PostListFooter(
                        isLoading = userProfileViewModel.isLoadingPosts,
                        hasMore = userProfileViewModel.hasMorePosts,
                        isEmpty = userProfileViewModel.userPosts.isEmpty()
                    )
                }
            }
        }
    }
}


@Composable
fun PostListFooter(
    isLoading: Boolean,
    hasMore: Boolean,
    isEmpty: Boolean,
    emptyMessage: String = "Nessun post pubblicato",
    endMessage: String = "Hai visto tutti i post 🎉"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
            !hasMore && !isEmpty -> {
                Text(
                    text = endMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            !hasMore && isEmpty -> {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==================== DIALOG MODIFICA PROFILO ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentUser: User,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, bio: String, dateOfBirth: String, picture: String?) -> Unit
) {
    var name by remember { mutableStateOf(currentUser.username ?: "") }
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

                LimitedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome",
                    maxLength = 15,
                    allowSpaces= false
                )

                LimitedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio",
                    maxLength = 100
                )

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
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Annulla")
            }
        }

        /* Altenativa
         IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                chiamo on Save
                                text = ""  pulisco
                            }
                        },
                        enabled = text.isNotBlank()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Invia",
                            tint = if (text.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
         */
    )
}

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
