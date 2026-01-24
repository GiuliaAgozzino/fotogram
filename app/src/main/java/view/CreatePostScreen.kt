package view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import model.LocationData

import view.common.LimitedTextField
import view.common.rememberImagePicker
import view.common.ErrorDialog
import view.location.LocationPermission
import viewModel.CreatePostViewModel
import viewModel.DataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    createPostViewModel: CreatePostViewModel,
    dataViewModel: DataViewModel,
    modifier: Modifier = Modifier,
    onBackToFeed: () -> Unit,
    onPostCreated: () -> Unit = {}
) {
    var contentText by remember { mutableStateOf("") }
    var contentPicture by remember { mutableStateOf<String?>(null) }

    var postLocation by remember { mutableStateOf<LocationData?>(null) }
    var showLocationPicker by remember { mutableStateOf(false) }

    val imagePicker = rememberImagePicker { base64 ->
        contentPicture = base64
    }

    val isLoading = createPostViewModel.isLoading
    val canPublish = contentText.isNotBlank() && contentPicture != null && !isLoading

    // Torna al feed quando il post è creato
    LaunchedEffect(createPostViewModel.postCreated) {
        if (createPostViewModel.postCreated) {
            // Aggiungi il post al DataViewModel
            createPostViewModel.createdPost?.let { post ->
                dataViewModel.addPost(post)
            }
            createPostViewModel.resetPostCreated()
            onPostCreated()
            onBackToFeed()
        }
    }

    // Dialog errore
    if (createPostViewModel.showError) {
        ErrorDialog(
            onDismiss = { createPostViewModel.clearError() },
            message = "Errore durante la pubblicazione. Riprova."
        )
    }

    if (showLocationPicker) {
        LocationPermission(
            onDismiss = { showLocationPicker = false },
            onLocationSelected = { point ->
                postLocation = LocationData(
                    latitude = point.latitude(),
                    longitude = point.longitude()
                )
                showLocationPicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuovo Post") },
                navigationIcon = {
                    IconButton(onClick = onBackToFeed, enabled = !isLoading) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { createPostViewModel.newPost(contentText, contentPicture!!, postLocation) },
                        enabled = canPublish
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Pubblica")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo testo
            LimitedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                label = "Scrivi qualcosa",
                maxLength = 100,
                enabled = !isLoading,
                singleLine = false,
                maxLines = 4
            )

            // Immagine
            if (contentPicture != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Base64Image(
                        base64 = contentPicture!!,
                        contentDescription = "Immagine post",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                TextButton(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = !isLoading
                ) {
                    Text("Cambia immagine")
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = !isLoading) { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Aggiungi immagine",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Aggiungi un'immagine",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Card posizione
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (postLocation != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (postLocation != null) "Posizione aggiunta" else "Nessuna posizione",
                            color = if (postLocation != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (postLocation != null) {
                        IconButton(
                            onClick = { postLocation = null },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Rimuovi posizione")
                        }
                    } else {
                        TextButton(
                            onClick = { showLocationPicker = true },
                            enabled = !isLoading
                        ) {
                            Text("Aggiungi")
                        }
                    }
                }
            }

            // Messaggio requisiti
            if (!canPublish && !isLoading) {
                Text(
                    text = when {
                        contentText.isBlank() && contentPicture == null -> "Scrivi qualcosa e aggiungi un'immagine per pubblicare"
                        contentText.isBlank() -> "Scrivi qualcosa per pubblicare"
                        contentPicture == null -> "Aggiungi un'immagine per pubblicare"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun Base64Image(
    base64: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap = remember(base64) {
        val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
