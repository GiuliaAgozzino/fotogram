package view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewModel.FeedViewModel

@Composable
fun FeedScreen(modifier: Modifier, feedViewModel: FeedViewModel) {

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Feed Screen")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "User ID: ${feedViewModel.userId ?: "N/A"}")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Session ID: ${feedViewModel.sessionId ?: "N/A"}")
    }
}