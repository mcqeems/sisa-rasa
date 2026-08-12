package com.mobile.sisarasa.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobile.sisarasa.ui.components.EmptyState
import com.mobile.sisarasa.ui.components.PostCard

@Composable
fun RiwayatTab(
    viewModel: RiwayatViewModel,
    onOpenPost: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val history by viewModel.history.collectAsStateWithLifecycle()

    if (history.isEmpty()) {
        EmptyState(text = "Belum ada riwayat klaim atau penjemputan.")
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(history, key = { it.id }) { post ->
            PostCard(post = post, onClick = { onOpenPost(post.id) })
        }
    }
}
