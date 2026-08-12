package com.mobile.sisarasa.data.local

import com.mobile.sisarasa.data.repository.PostRepository
import com.mobile.sisarasa.domain.PostLogic.canBeClaimedBy
import com.mobile.sisarasa.domain.PostLogic.claim
import com.mobile.sisarasa.domain.PostLogic.markSoldOut
import com.mobile.sisarasa.domain.PostLogic.sortedByNewest
import com.mobile.sisarasa.domain.model.Post
import com.mobile.sisarasa.domain.model.PostStatus
import com.mobile.sisarasa.domain.model.PostType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// ponytail: in-memory realtime-ish store; re-emits on every mutation so the feed updates live.
class LocalPostRepository(
    initial: List<Post> = SampleData.posts,
) : PostRepository {

    private val _posts = MutableStateFlow(initial)
    private val posts get() = _posts.value

    override fun observePosts(): Flow<List<Post>> = _posts

    override suspend fun add(post: Post): Result<Unit> {
        _posts.value = (posts + post).sortedByNewest()
        return Result.success(Unit)
    }

    override suspend fun update(post: Post): Result<Unit> {
        _posts.value = posts.map { if (it.id == post.id) post else it }.sortedByNewest()
        return Result.success(Unit)
    }

    override suspend fun delete(postId: String): Result<Unit> {
        _posts.value = posts.filterNot { it.id == postId }
        return Result.success(Unit)
    }

    override suspend fun claim(postId: String, userId: String): Result<Unit> {
        _posts.value = posts.map { post ->
            if (post.id == postId && post.canBeClaimedBy(userId)) post.claim(userId) else post
        }
        return Result.success(Unit)
    }

    override suspend fun markSoldOut(postId: String): Result<Unit> {
        _posts.value = posts.map { post ->
            if (post.id == postId && post.type == PostType.FLASH_RESCUE && post.status == PostStatus.AVAILABLE) {
                post.markSoldOut()
            } else {
                post
            }
        }
        return Result.success(Unit)
    }
}
