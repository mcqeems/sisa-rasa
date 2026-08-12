package com.mobile.sisarasa.data.repository

import com.mobile.sisarasa.domain.model.AppUser
import com.mobile.sisarasa.domain.model.UserRole
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AppUser?>

    suspend fun login(email: String, password: String): Result<AppUser>

    suspend fun register(email: String, password: String, name: String, role: UserRole): Result<AppUser>

    suspend fun logout()
}
