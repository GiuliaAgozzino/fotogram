package utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.PostWithAuthor

/**
 * Classe helper per gestire la paginazione dei post.
 * Elimina la duplicazione della logica di caricamento paginato nei ViewModel.
 *
 * @param tag Tag per i log
 * @param pageSize Numero di elementi per pagina (default 10)
 * @param coroutineScope Scope per le coroutine (tipicamente viewModelScope)
 * @param onLoadingChange Callback quando cambia lo stato di loading
 * @param onPostsChange Callback quando cambia la lista dei post
 * @param onHasMoreChange Callback quando cambia hasMore
 * @param onError Callback in caso di errore
 * @param fetchPosts Funzione suspend che recupera i post dato un maxPostId
 */
class PostsPaginator(
    private val tag: String,
    private val pageSize: Int = 10,
    private val initialMaxPostId: Int = 0,
    private val coroutineScope: CoroutineScope,
    private val onLoadingChange: (Boolean) -> Unit,
    private val onPostsChange: (List<PostWithAuthor>) -> Unit,
    private val onHasMoreChange: (Boolean) -> Unit,
    private val onError: () -> Unit,
    private val fetchPosts: suspend (maxPostId: Int) -> Result<List<PostWithAuthor>>
) {
    private var currentPosts: List<PostWithAuthor> = emptyList()
    private var currentMaxPostId: Int = initialMaxPostId
    private var hasMore: Boolean = true
    private var isLoading: Boolean = false


    fun loadMore() {
        if (isLoading || !hasMore) return

        coroutineScope.launch {
            isLoading = true
            onLoadingChange(true)

            Log.d(tag, "Caricamento post: maxPostId=$currentMaxPostId, hasMore=$hasMore")

            try {
                val result = fetchPosts(currentMaxPostId)

                if (result.isSuccess) {
                    val newPosts = result.getOrNull() ?: emptyList()

                    Log.d(tag, "Ricevuti ${newPosts.size} post")

                    when {
                        newPosts.isEmpty() -> {
                            hasMore = false
                            onHasMoreChange(false)
                            Log.d(tag, "Fine post: lista vuota")
                        }
                        newPosts.size < pageSize -> {
                            currentPosts = currentPosts + newPosts
                            onPostsChange(currentPosts)
                            hasMore = false
                            onHasMoreChange(false)
                            Log.d(tag, "Fine post: ricevuti ${newPosts.size} < $pageSize")
                        }
                        else -> {
                            currentPosts = currentPosts + newPosts
                            onPostsChange(currentPosts)
                            currentMaxPostId = newPosts.last().postId - 1

                            if (currentMaxPostId <= 0) {
                                hasMore = false
                                onHasMoreChange(false)
                                Log.d(tag, "Fine post: maxPostId <= 0")
                            }

                            Log.d(tag, "Caricati ${newPosts.size} post. Totale: ${currentPosts.size}, nextMaxPostId=$currentMaxPostId")
                        }
                    }
                } else {
                    onError()
                    Log.e(tag, "Errore: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                onError()
                Log.e(tag, "Eccezione caricamento post", e)
            } finally {
                isLoading = false
                onLoadingChange(false)
            }
        }
    }


    fun reset() {
        currentPosts = emptyList()
        currentMaxPostId = initialMaxPostId
        hasMore = true
        isLoading = false
        onPostsChange(emptyList())
        onHasMoreChange(true)
    }

}
