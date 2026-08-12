package com.mobile.sisarasa.data.supabase

import android.util.Log
import com.mobile.sisarasa.BuildConfig
import com.mobile.sisarasa.data.repository.StorageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ponytail: Supabase Storage for food photos (free tier). URL + anon key are
// client-safe by design and live in BuildConfig like MAPS_API_KEY.
class SupabaseStorageRepository : StorageRepository {

    override suspend fun uploadPhoto(bytes: ByteArray): Result<String?> {
        if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_ANON_KEY.isBlank()) {
            Log.w("SisaRasa", "Supabase URL or ANON_KEY is blank! Skipping upload.")
            return Result.success(null)
        }
        Log.d("SisaRasa", "Supabase upload starting... Bytes count: ${bytes.size}, Target bucket: $BUCKET")
        return runCatching {
            withContext(Dispatchers.IO) {
                val path = "u/${UUID.randomUUID()}.jpg"
                client.storage.from(BUCKET).upload(path, bytes) {
                    upsert = true
                }
                val url = client.storage.from(BUCKET).publicUrl(path)
                Log.d("SisaRasa", "Supabase upload SUCCESS: $url")
                url
            }
        }.onFailure { e ->
            Log.e("SisaRasa", "Supabase upload FAILED: ${e.message}", e)
        }
    }

    private companion object {
        const val BUCKET = "posts"
        val client: SupabaseClient by lazy {
            createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
                install(Storage)
            }
        }
    }
}