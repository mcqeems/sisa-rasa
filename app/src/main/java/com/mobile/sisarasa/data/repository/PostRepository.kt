package com.mobile.sisarasa.data.repository

import com.mobile.sisarasa.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun observePosts(): Flow<List<Post>>

    suspend fun add(post: Post): Result<Unit>

    suspend fun update(post: Post): Result<Unit>

    suspend fun delete(postId: String): Result<Unit>

    suspend fun claim(postId: String, userId: String): Result<Unit>

    suspend fun markSoldOut(postId: String): Result<Unit>
}
