package com.mobile.sisarasa.data.firebase

import com.mobile.sisarasa.BuildConfig
import com.mobile.sisarasa.data.repository.AuthRepository
import com.mobile.sisarasa.domain.model.AppUser
import com.mobile.sisarasa.domain.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL).getReference("users")
    private val _authState = MutableStateFlow(currentUser())

    init {
        auth.addAuthStateListener {
            _authState.value = currentUser()
        }
    }

    override val authState: StateFlow<AppUser?> = _authState

    override suspend fun login(email: String, password: String): Result<AppUser> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        auth.currentUser?.reload()?.await()
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Akun tidak ditemukan.")
        val dbUser = fetchUserFromDb(uid)
        val user = dbUser ?: currentUser() ?: throw IllegalStateException("Gagal memuat data pengguna.")
        _authState.value = user
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun register(email: String, password: String, name: String, role: UserRole): Result<AppUser> = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Gagal membuat akun.")
        
        // ponytail: Save user profile to Realtime Database users/{uid}
        usersRef.child(uid).setValue(
            mapOf(
                "name" to name,
                "email" to email,
                "role" to role.name,
            )
        ).await()

        auth.currentUser?.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName("$name|${role.name}").build()
        )?.await()
        auth.currentUser?.reload()?.await()

        val user = AppUser(id = uid, email = email, name = name, role = role)
        _authState.value = user
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun logout() {
        auth.signOut()
        _authState.value = null
    }

    private suspend fun fetchUserFromDb(uid: String): AppUser? = try {
        val snapshot = usersRef.child(uid).get().await()
        if (snapshot.exists()) {
            val name = snapshot.child("name").value as? String ?: ""
            val email = snapshot.child("email").value as? String ?: ""
            val roleStr = snapshot.child("role").value as? String ?: "PERSONAL"
            val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.PERSONAL }
            AppUser(id = uid, email = email, name = name, role = role)
        } else null
    } catch (_: Exception) {
        null
    }

    private fun currentUser(): AppUser? {
        val u: FirebaseUser = auth.currentUser ?: return null
        val displayName = u.displayName
        val role = try {
            UserRole.valueOf(displayName?.substringAfter('|')?.trim() ?: "PERSONAL")
        } catch (_: IllegalArgumentException) {
            UserRole.PERSONAL
        }
        val rawName = displayName?.substringBefore('|')?.trim().orEmpty()
        val name = if (rawName.isNotBlank()) rawName else u.email?.substringBefore('@')?.trim().orEmpty().ifBlank { "Pengguna" }
        return AppUser(
            id = u.uid,
            email = u.email.orEmpty(),
            name = name,
            role = role,
        )
    }
}
