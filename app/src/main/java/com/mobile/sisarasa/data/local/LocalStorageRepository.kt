package com.mobile.sisarasa.data.local

import com.mobile.sisarasa.data.repository.StorageRepository

class LocalStorageRepository : StorageRepository {
    override suspend fun uploadPhoto(bytes: ByteArray): Result<String?> =
        Result.success(null)
}
