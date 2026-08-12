package com.mobile.sisarasa.ui.screens.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    postId: String?,
    type: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel = com.mobile.sisarasa.ui.appViewModel { EditPostViewModel(it, postId, type) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri
        uri?.let {
            val bytes = compressImageBytes(context, it)
            if (bytes != null) {
                android.util.Log.d("SisaRasa", "Picked image compressed: ${bytes.size} bytes")
                viewModel.onPhotoPicked(bytes)
            } else {
                android.util.Log.e("SisaRasa", "Failed to compress picked image")
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (postId.isNullOrBlank()) "Tambah Postingan" else "Edit Postingan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (state.type == com.mobile.sisarasa.domain.model.PostType.DONASI) {
                Text(
                    "Postingan Donasi (gratis)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    "Postingan Flash Rescue (diskon)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            val currentPhotoModel: Any? = photoUri ?: state.photoBytes ?: state.existing?.photoUrl

            if (currentPhotoModel != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        AsyncImage(
                            model = currentPhotoModel,
                            contentDescription = "Pratinjau Foto Makanan",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Ganti Foto")
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    photoUri = null
                                    viewModel.onPhotoRemoved()
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(4.dp))
                                Text("Hapus Foto", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Pilih Foto Makanan", modifier = Modifier.padding(start = 8.dp))
                }
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Nama Makanan") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            if (state.type == com.mobile.sisarasa.domain.model.PostType.FLASH_RESCUE) {
                OutlinedTextField(
                    value = state.originalPrice,
                    onValueChange = viewModel::onOriginalPriceChange,
                    label = { Text("Harga Awal (Rp)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
                OutlinedTextField(
                    value = state.price,
                    onValueChange = viewModel::onPriceChange,
                    label = { Text("Harga Jual (Rp)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
            OutlinedTextField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Lokasi / Alamat Penjemputan") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )

            state.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Button(
                onClick = viewModel::save,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                Text(if (state.saving) "Menyimpan..." else "Simpan")
            }
            if (state.saving) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

private fun compressImageBytes(context: android.content.Context, uri: Uri): ByteArray? = try {
    val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        android.graphics.BitmapFactory.decodeStream(stream, null, options)
    }
    var sampleSize = 1
    val maxDimension = 1024
    while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
        sampleSize *= 2
    }
    val decodeOptions = android.graphics.BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
        android.graphics.BitmapFactory.decodeStream(stream, null, decodeOptions)
    } ?: return null
    val outputStream = java.io.ByteArrayOutputStream()
    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
    outputStream.toByteArray()
} catch (e: Exception) {
    android.util.Log.e("SisaRasa", "Failed to compress image", e)
    null
}