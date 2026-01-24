package view.common

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*


@Composable
fun InfiniteScrollEffect(
    listState: LazyListState,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    threshold: Int = 3,
    tag: String = "InfiniteScroll"
) {
    val lastVisibleIndex = remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
    }

    LaunchedEffect(lastVisibleIndex.value) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val total = listState.layoutInfo.totalItemsCount
        val triggerThreshold = total - threshold

        if (lastVisible != null && lastVisible >= triggerThreshold && hasMore) {
            Log.d(tag, "Trigger loadMore - lastVisible=$lastVisible, total=$total")
            onLoadMore()
        }
    }
}