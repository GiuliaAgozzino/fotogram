package view

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.FeedViewModel
import view.common.ErrorDialog
import view.common.LoadingIndicator
import view.common.PostItem

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    feedViewModel: FeedViewModel,
) {
    // Dialog errore
    if (feedViewModel.showError) {
        ErrorDialog(
            onDismiss = { feedViewModel.clearError() },
            onRetry = { feedViewModel.refresh() }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            feedViewModel.isLoading -> LoadingIndicator()

            feedViewModel.post != null -> {
                PostItem(
                    post = feedViewModel.post!!,
                    onAuthorClick = { authorId ->
                        // TODO: apri mappa
                    },
                    onLocationClick = { postId ->
                        // TODO: apri mappa
                    }
                )
            }

            else -> {
                androidx.compose.material3.Text("Nessun post disponibile")
            }
        }
    }
}