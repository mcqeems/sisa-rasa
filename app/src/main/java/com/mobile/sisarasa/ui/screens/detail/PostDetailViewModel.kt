package com.mobile.sisarasa.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sisarasa.data.repository.PostRepository
import com.mobile.sisarasa.di.AppContainer
import com.mobile.sisarasa.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val post: Post? = null,
    val currentUserId: String? = null,
    val actionMessage: String? = null,
)

class PostDetailViewModel(
    container: AppContainer,
    postId: String,
) : ViewModel() {

    private val postRepo: PostRepository = container.postRepository

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.postRepository.observePosts().collect { posts ->
                _uiState.value = _uiState.value.copy(post = posts.find { it.id == postId })
            }
        }
        viewModelScope.launch {
            container.authRepository.authState.collect { user ->
                _uiState.value = _uiState.value.copy(currentUserId = user?.id)
            }
        }
    }

    fun claim() {
        val userId = _uiState.value.currentUserId ?: return
        mutate { postRepo.claim(it.id, userId) }
    }

    fun markSoldOut() = mutate { postRepo.markSoldOut(it.id) }

    fun delete() = mutate { postRepo.delete(it.id) }

    private fun mutate(block: suspend (Post) -> Result<Unit>) {
        val post = _uiState.value.post ?: return
        viewModelScope.launch {
            block(post)
                .onSuccess { _uiState.value = _uiState.value.copy(actionMessage = null) }
                .onFailure { _uiState.value = _uiState.value.copy(actionMessage = it.message) }
        }
    }
}
