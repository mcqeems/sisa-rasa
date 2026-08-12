package com.mobile.sisarasa.data.repository

interface StorageRepository {
    suspend fun uploadPhoto(bytes: ByteArray): Result<String?>
}
