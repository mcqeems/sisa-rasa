package com.mobile.sisarasa.data.firebase

import android.util.Log
import com.mobile.sisarasa.BuildConfig
import com.mobile.sisarasa.data.repository.PostRepository
import com.mobile.sisarasa.domain.PostLogic.canBeClaimedBy
import com.mobile.sisarasa.domain.PostLogic.sortedByNewest
import com.mobile.sisarasa.domain.model.Location
import com.mobile.sisarasa.domain.model.Post
import com.mobile.sisarasa.domain.model.PostStatus
import com.mobile.sisarasa.domain.model.PostType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebasePostRepository : PostRepository {

    private val postsRef = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL).getReference("posts")

    override fun observePosts(): Flow<List<Post>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.toPost() }.sortedByNewest()
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        postsRef.addValueEventListener(listener)
        awaitClose { postsRef.removeEventListener(listener) }
    }

    override suspend fun add(post: Post): Result<Unit> = try {
        Log.d("SisaRasa", "Firebase RTDB add starting for post ID: ${post.id}")
        postsRef.child(post.id).setValue(post.toMap()).await()
        Log.d("SisaRasa", "Firebase RTDB add SUCCESS for post ID: ${post.id}")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("SisaRasa", "Firebase RTDB add FAILED for post ID: ${post.id}", e)
        Result.failure(e)
    }

    override suspend fun update(post: Post): Result<Unit> = try {
        Log.d("SisaRasa", "Firebase RTDB update starting for post ID: ${post.id}")
        postsRef.child(post.id).setValue(post.toMap()).await()
        Log.d("SisaRasa", "Firebase RTDB update SUCCESS for post ID: ${post.id}")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("SisaRasa", "Firebase RTDB update FAILED for post ID: ${post.id}", e)
        Result.failure(e)
    }

    override suspend fun delete(postId: String): Result<Unit> = try {
        postsRef.child(postId).removeValue().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun claim(postId: String, userId: String): Result<Unit> {
        val post = postsRef.child(postId).get().await().toPost()
            ?: return Result.failure(IllegalStateException("Postingan tidak ditemukan."))
        if (!post.canBeClaimedBy(userId)) {
            return Result.failure(IllegalStateException("Postingan sudah tidak tersedia."))
        }
        return try {
            postsRef.child(postId).updateChildren(
                mapOf("status" to PostStatus.CLAIMED.name, "claimerId" to userId)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markSoldOut(postId: String): Result<Unit> = try {
        postsRef.child(postId).updateChildren(mapOf("status" to PostStatus.SOLD_OUT.name)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun DataSnapshot.toPost(): Post? {
        val type = child("type").value as? String ?: return null
        val loc = child("location")
        return Post(
            id = key ?: return null,
            type = PostType.valueOf(type),
            title = child("title").value as? String ?: "",
            description = child("description").value as? String ?: "",
            photoUrl = child("photoUrl").value as? String,
            price = (child("price").value as? Number)?.toLong() ?: 0L,
            originalPrice = (child("originalPrice").value as? Number)?.toLong(),
            status = (child("status").value as? String)?.let { PostStatus.valueOf(it) } ?: PostStatus.AVAILABLE,
            location = Location(
                address = loc.child("address").value as? String ?: "",
                latitude = (loc.child("latitude").value as? Number)?.toDouble() ?: 0.0,
                longitude = (loc.child("longitude").value as? Number)?.toDouble() ?: 0.0,
            ),
            donorId = child("donorId").value as? String ?: "",
            donorName = child("donorName").value as? String ?: "",
            claimerId = child("claimerId").value as? String,
            createdAt = (child("createdAt").value as? Number)?.toLong() ?: 0L,
        )
    }

    private fun Post.toMap(): Map<String, Any> = mapOf(
        "type" to type.name,
        "title" to title,
        "description" to description,
        "photoUrl" to (photoUrl ?: ""),
        "price" to price,
        "originalPrice" to (originalPrice ?: 0L),
        "status" to status.name,
        "location" to mapOf(
            "address" to location.address,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
        ),
        "donorId" to donorId,
        "donorName" to donorName,
        "claimerId" to (claimerId ?: ""),
        "createdAt" to createdAt,
    )
}
