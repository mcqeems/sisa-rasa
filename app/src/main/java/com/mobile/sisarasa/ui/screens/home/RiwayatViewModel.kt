package com.mobile.sisarasa.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sisarasa.di.AppContainer
import com.mobile.sisarasa.domain.PostLogic.sortedByNewest
import com.mobile.sisarasa.domain.model.Post
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class RiwayatViewModel(container: AppContainer) : ViewModel() {

    val history: StateFlow<List<Post>> = combine(
        container.authRepository.authState,
        container.postRepository.observePosts(),
    ) { user, posts ->
        if (user == null) emptyList()
        else posts.filter {
            it.claimerId == user.id ||
                (it.donorId == user.id && it.status != com.mobile.sisarasa.domain.model.PostStatus.AVAILABLE)
        }.sortedByNewest()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
