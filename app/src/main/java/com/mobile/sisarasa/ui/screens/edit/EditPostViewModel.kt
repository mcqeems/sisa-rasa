package com.mobile.sisarasa.ui.screens.edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sisarasa.data.repository.AuthRepository
import com.mobile.sisarasa.data.repository.PostRepository
import com.mobile.sisarasa.data.repository.StorageRepository
import com.mobile.sisarasa.di.AppContainer
import com.mobile.sisarasa.domain.model.Location
import com.mobile.sisarasa.domain.model.Post
import com.mobile.sisarasa.domain.model.PostType
import com.mobile.sisarasa.domain.model.UserRole
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditUiState(
    val existing: Post? = null,
    val type: PostType? = null,
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val originalPrice: String = "",
    val address: String = "",
    val photoBytes: ByteArray? = null,
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class EditPostViewModel(
    container: AppContainer,
    postId: String?,
    typeName: String?,
) : ViewModel() {

    private val postRepo: PostRepository = container.postRepository
    private val storageRepo: StorageRepository = container.storageRepository
    private val authRepo: AuthRepository = container.authRepository

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (postId != null && postId.isNotBlank()) {
                postRepo.observePosts().collect { posts ->
                    val existing = posts.find { it.id == postId } ?: return@collect
                    _uiState.value = EditUiState(
                        existing = existing,
                        type = existing.type,
                        title = existing.title,
                        description = existing.description,
                        price = existing.price.toString(),
                        originalPrice = existing.originalPrice?.toString() ?: "",
                        address = existing.location.address,
                    )
                }
            } else {
                val type = PostType.valueOf(typeName ?: PostType.DONASI.name)
                _uiState.value = EditUiState(type = type)
            }
        }
    }

    fun onTitleChange(v: String) = update { it.copy(title = v) }
    fun onDescriptionChange(v: String) = update { it.copy(description = v) }
    fun onPriceChange(v: String) = update { it.copy(price = v) }
    fun onOriginalPriceChange(v: String) = update { it.copy(originalPrice = v) }
    fun onAddressChange(v: String) = update { it.copy(address = v) }
    fun onPhotoPicked(bytes: ByteArray) = update { it.copy(photoBytes = bytes) }
    fun onPhotoRemoved() = update { it.copy(photoBytes = null, existing = it.existing?.copy(photoUrl = null)) }

    fun save() {
        val state = _uiState.value
        val user = authRepo.authState.value
        if (user == null) {
            _uiState.value = state.copy(error = "Anda harus login terlebih dahulu.")
            return
        }
        if (state.title.isBlank() || state.address.isBlank()) {
            _uiState.value = state.copy(error = "Nama makanan dan alamat wajib diisi.")
            return
        }
        val type = state.existing?.type ?: state.type ?: PostType.DONASI
        val roleAllowsType = (type == PostType.DONASI && user.role == UserRole.PERSONAL) ||
            (type == PostType.FLASH_RESCUE && user.role == UserRole.MITRA_BISNIS)
        if (!roleAllowsType) {
            _uiState.value = state.copy(error = "Jenis akun Anda tidak sesuai dengan jenis postingan ini.")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(saving = true, error = null)
            Log.d("SisaRasa", "Starting save()... photoBytes present: ${state.photoBytes != null}")
            try {
                val photoUrl: String?
                if (state.photoBytes != null) {
                    Log.d("SisaRasa", "Uploading photo (${state.photoBytes.size} bytes) to Supabase...")
                    val uploadResult = try {
                        kotlinx.coroutines.withTimeout(15_000L) {
                            storageRepo.uploadPhoto(state.photoBytes)
                        }
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        Log.e("SisaRasa", "Supabase upload timed out (15s)")
                        _uiState.value = state.copy(
                            saving = false,
                            error = "Waktu habis saat mengunggah foto ke Supabase. Periksa koneksi internet Anda.",
                        )
                        return@launch
                    }

                    if (uploadResult.isFailure) {
                        val err = uploadResult.exceptionOrNull()?.message ?: "Gagal mengunggah foto ke Supabase."
                        Log.e("SisaRasa", "Supabase upload failed: $err")
                        _uiState.value = state.copy(
                            saving = false,
                            error = "Gagal unggah foto: $err",
                        )
                        return@launch
                    }
                    photoUrl = uploadResult.getOrNull()
                    Log.d("SisaRasa", "Photo upload success. URL: $photoUrl")
                } else {
                    photoUrl = state.existing?.photoUrl
                }

                val location = state.existing?.location ?: Location(
                    address = state.address,
                    // ponytail: no geocoding in mock mode; coords fixed. Replace with geocoder/places later.
                    latitude = -7.7956,
                    longitude = 110.3695,
                ).copy(address = state.address)

                val post = state.existing?.copy(
                    title = state.title,
                    description = state.description,
                    price = state.price.toLongOrNull() ?: 0L,
                    originalPrice = state.originalPrice.toLongOrNull(),
                    location = location,
                    photoUrl = photoUrl,
                ) ?: Post(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    title = state.title,
                    description = state.description,
                    price = state.price.toLongOrNull() ?: 0L,
                    originalPrice = state.originalPrice.toLongOrNull(),
                    location = location,
                    donorId = user.id,
                    donorName = user.name,
                    createdAt = System.currentTimeMillis(),
                    photoUrl = photoUrl,
                )

                Log.d("SisaRasa", "Saving post to Database: ${post.title} (ID: ${post.id})...")
                val dbResult = try {
                    kotlinx.coroutines.withTimeout(10_000L) {
                        if (state.existing != null) postRepo.update(post) else postRepo.add(post)
                    }
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    Log.e("SisaRasa", "Database save timed out (10s)")
                    _uiState.value = state.copy(
                        saving = false,
                        error = "Waktu habis saat menyimpan data ke database.",
                    )
                    return@launch
                }

                dbResult.onSuccess {
                    Log.d("SisaRasa", "Post saved successfully!")
                    _uiState.value = _uiState.value.copy(saving = false, saved = true)
                }.onFailure {
                    Log.e("SisaRasa", "Database save failed: ${it.message}")
                    _uiState.value = _uiState.value.copy(saving = false, error = it.message ?: "Gagal menyimpan postingan.")
                }
            } catch (e: Exception) {
                Log.e("SisaRasa", "Unexpected exception during save()", e)
                _uiState.value = _uiState.value.copy(
                    saving = false,
                    error = e.message ?: "Terjadi kesalahan saat menyimpan postingan.",
                )
            }
        }
    }

    private fun update(block: (EditUiState) -> EditUiState) {
        _uiState.value = block(_uiState.value)
    }
}
