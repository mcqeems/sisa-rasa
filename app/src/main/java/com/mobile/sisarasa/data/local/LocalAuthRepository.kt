package com.mobile.sisarasa.data.local

import com.mobile.sisarasa.data.repository.AuthRepository
import com.mobile.sisarasa.domain.model.AppUser
import com.mobile.sisarasa.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ponytail: in-memory auth for demo. Login succeeds for any registered account.
class LocalAuthRepository : AuthRepository {
    private val _authState = MutableStateFlow<AppUser?>(null)
    override val authState: StateFlow<AppUser?> = _authState

    private val users = mutableMapOf<String, AppUser>()

    override suspend fun login(email: String, password: String): Result<AppUser> {
        val user = users[email.trim().lowercase()]
            ?: return Result.failure(IllegalStateException("Akun belum terdaftar. Silakan daftar dulu."))
        if (password.isBlank()) return Result.failure(IllegalStateException("Kata sandi tidak boleh kosong."))
        _authState.value = user
        return Result.success(user)
    }

    override suspend fun register(email: String, password: String, name: String, role: UserRole): Result<AppUser> {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || password.isBlank() || name.isBlank()) {
            return Result.failure(IllegalStateException("Semua kolom wajib diisi."))
        }
        if (users.containsKey(normalized)) {
            return Result.failure(IllegalStateException("Email sudah terdaftar."))
        }
        val user = AppUser(id = "u-${normalized.hashCode()}", email = normalized, name = name.trim(), role = role)
        users[normalized] = user
        _authState.value = user
        return Result.success(user)
    }

    override suspend fun logout() {
        _authState.value = null
    }
}
