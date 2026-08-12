package com.mobile.sisarasa.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sisarasa.data.repository.AuthRepository
import com.mobile.sisarasa.data.repository.PostRepository
import com.mobile.sisarasa.di.AppContainer
import com.mobile.sisarasa.domain.PostLogic.filterBy
import com.mobile.sisarasa.domain.PostLogic.sortedByNewest
import com.mobile.sisarasa.domain.model.AppUser
import com.mobile.sisarasa.domain.model.FeedFilter
import com.mobile.sisarasa.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val currentUser: AppUser? = null,
    val filter: FeedFilter = FeedFilter.SEMUA,
    val posts: List<Post> = emptyList(),
)

class HomeViewModel(container: AppContainer) : ViewModel() {

    private val postRepo: PostRepository = container.postRepository

    private val _filter = MutableStateFlow(FeedFilter.SEMUA)
    val filter: StateFlow<FeedFilter> = _filter.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        container.authRepository.authState,
        _filter,
        postRepo.observePosts(),
    ) { user, filter, posts ->
        HomeUiState(
            currentUser = user,
            filter = filter,
            posts = posts.filterBy(filter).sortedByNewest(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setFilter(filter: FeedFilter) {
        _filter.value = filter
    }
}
