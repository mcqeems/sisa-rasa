package com.mobile.sisarasa.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobile.sisarasa.domain.model.FeedFilter
import com.mobile.sisarasa.domain.model.PostType
import com.mobile.sisarasa.domain.model.UserRole
import com.mobile.sisarasa.ui.appViewModel
import com.mobile.sisarasa.ui.components.EmptyState
import com.mobile.sisarasa.ui.components.FilterTabs
import com.mobile.sisarasa.ui.components.PostCard

private enum class HomeTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    BERANDA("Beranda", Icons.Filled.Home),
    RIWAYAT("Riwayat", Icons.Filled.List),
    PROFIL("Profil", Icons.Filled.Person),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenPost: (String) -> Unit,
    onCreatePost: (PostType) -> Unit,
) {
    val viewModel: HomeViewModel = appViewModel { HomeViewModel(it) }
    val riwayatViewModel: RiwayatViewModel = appViewModel { RiwayatViewModel(it) }
    val authViewModel: com.mobile.sisarasa.ui.screens.auth.AuthViewModel = appViewModel { com.mobile.sisarasa.ui.screens.auth.AuthViewModel(it) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(HomeTab.BERANDA) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("SisaRasa") }) },
        floatingActionButton = {
            if (tab == HomeTab.BERANDA) {
                FloatingActionButton(onClick = {
                    val type = when (state.currentUser?.role) {
                        UserRole.MITRA_BISNIS -> PostType.FLASH_RESCUE
                        else -> PostType.DONASI
                    }
                    onCreatePost(type)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Postingan")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (tab) {
            HomeTab.BERANDA -> FeedTab(
                state = state,
                filter = state.filter,
                onFilter = viewModel::setFilter,
                onOpenPost = onOpenPost,
                modifier = Modifier.padding(innerPadding),
            )
            HomeTab.RIWAYAT -> RiwayatTab(
                viewModel = riwayatViewModel,
                onOpenPost = onOpenPost,
                modifier = Modifier.padding(innerPadding),
            )
            HomeTab.PROFIL -> ProfilTab(
                name = state.currentUser?.name.orEmpty(),
                role = state.currentUser?.role,
                email = state.currentUser?.email.orEmpty(),
                onLogout = authViewModel::logout,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun FeedTab(
    state: HomeUiState,
    filter: FeedFilter,
    onFilter: (FeedFilter) -> Unit,
    onOpenPost: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        FilterTabs(selected = filter, onSelect = onFilter)
        if (state.posts.isEmpty()) {
            EmptyState(text = "Belum ada postingan. Tambahkan yang pertama!")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.posts, key = { it.id }) { post ->
                    PostCard(post = post, onClick = { onOpenPost(post.id) })
                }
            }
        }
    }
}
