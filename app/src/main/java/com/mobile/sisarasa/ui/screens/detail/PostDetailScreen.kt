package com.mobile.sisarasa.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mobile.sisarasa.domain.PostLogic.canBeClaimedBy
import com.mobile.sisarasa.domain.PostLogic.canBeManagedBy
import com.mobile.sisarasa.domain.model.PostStatus
import com.mobile.sisarasa.domain.model.PostType
import com.mobile.sisarasa.ui.components.PickupLocationCard
import com.mobile.sisarasa.ui.components.StatusChip
import com.mobile.sisarasa.ui.components.priceLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
) {
    val viewModel = com.mobile.sisarasa.ui.appViewModel { PostDetailViewModel(it, postId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val post = state.post

    // ponytail: deleted posts vanish from the stream -> navigate back once.
    var wasVisible by remember { mutableStateOf(false) }
    LaunchedEffect(post) {
        if (post != null) wasVisible = true
        else if (wasVisible) onDeleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (post == null) {
            Text(
                text = "Postingan tidak ditemukan.",
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            return@Scaffold
        }

        val currentUser = state.currentUserId
        val isOwner = post.donorId == currentUser
        val canClaim = currentUser != null && post.canBeClaimedBy(currentUser)
        val canManage = isOwner && post.canBeManagedBy(currentUser ?: "")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            if (post.photoUrl.isNullOrBlank()) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Tidak ada foto", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                AsyncImage(
                    model = post.photoUrl,
                    contentDescription = post.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
            }

            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(post.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    StatusChip(post)
                }
                Text(
                    text = post.priceLabel(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "Dari: ${post.donorName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                if (post.description.isNotBlank()) {
                    Text(
                        text = post.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }

                PickupLocationCard(post.location, modifier = Modifier.padding(top = 16.dp))

                state.actionMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                ) {
                    if (canClaim) {
                        Button(onClick = viewModel::claim, modifier = Modifier.fillMaxWidth()) {
                            Text(if (post.type == PostType.DONASI) "Klaim Gratis" else "Klaim & Ambil")
                        }
                    }
                    if (post.type == PostType.FLASH_RESCUE && isOwner && post.status == PostStatus.AVAILABLE) {
                        OutlinedButton(onClick = viewModel::markSoldOut, modifier = Modifier.fillMaxWidth()) {
                            Text("Tandai Habis Terjual")
                        }
                    }
                    if (canManage) {
                        OutlinedButton(onClick = { onEdit(post.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Edit Postingan")
                        }
                        OutlinedButton(onClick = { viewModel.delete() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Hapus")
                        }
                    }
                }
            }
        }
    }
}
