package view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import viewModel.FeedViewModel

@Composable
fun FeedScreen(modifier: Modifier, viewModel: FeedViewModel, userId: Int?, sessionId: String? ) {

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TextField(
            value = "",
            onValueChange = { /* gestisci input */ },
            label = { Text("FeedScreen ${userId}, ${sessionId}") }, // opzionale, se vuoi un'etichetta sopra
            modifier = modifier.fillMaxWidth(0.8f)
        )
    }
}