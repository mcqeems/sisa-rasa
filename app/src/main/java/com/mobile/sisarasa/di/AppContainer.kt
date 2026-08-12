package com.mobile.sisarasa.di

import android.content.Context
import com.mobile.sisarasa.BuildConfig
import com.mobile.sisarasa.data.firebase.FirebaseAuthRepository
import com.mobile.sisarasa.data.firebase.FirebasePostRepository
import com.mobile.sisarasa.data.local.LocalAuthRepository
import com.mobile.sisarasa.data.local.LocalPostRepository
import com.mobile.sisarasa.data.local.LocalStorageRepository
import com.mobile.sisarasa.data.repository.AuthRepository
import com.mobile.sisarasa.data.repository.PostRepository
import com.mobile.sisarasa.data.repository.StorageRepository
import com.mobile.sisarasa.data.supabase.SupabaseStorageRepository
import com.google.firebase.FirebaseApp

// ponytail: manual DI instead of Hilt; Firebase enabled in prod config only.
class AppContainer(context: Context) {

    private val firebaseReady = BuildConfig.FIREBASE_ENABLED && FirebaseApp.initializeApp(context) != null

    val authRepository: AuthRepository =
        if (firebaseReady) FirebaseAuthRepository() else LocalAuthRepository()

    val postRepository: PostRepository =
        if (firebaseReady) FirebasePostRepository() else LocalPostRepository()

    // ponytail: photos go to (free) Supabase Storage; fall back to local (no photo)
    // only when the Supabase keys are missing.
    val storageRepository: StorageRepository =
        if (BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            SupabaseStorageRepository()
        } else {
            LocalStorageRepository()
        }
}