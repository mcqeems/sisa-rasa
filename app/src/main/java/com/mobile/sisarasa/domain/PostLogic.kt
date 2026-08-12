package com.mobile.sisarasa.domain

import com.mobile.sisarasa.domain.model.FeedFilter
import com.mobile.sisarasa.domain.model.Post
import com.mobile.sisarasa.domain.model.PostStatus
import com.mobile.sisarasa.domain.model.PostType

object PostLogic {

    fun Post.canBeClaimedBy(userId: String): Boolean =
        status == PostStatus.AVAILABLE && donorId != userId

    fun Post.canBeManagedBy(userId: String): Boolean =
        donorId == userId && status == PostStatus.AVAILABLE

    fun Post.claim(userId: String): Post =
        if (canBeClaimedBy(userId)) copy(status = PostStatus.CLAIMED, claimerId = userId) else this

    fun Post.markSoldOut(): Post =
        copy(status = PostStatus.SOLD_OUT)

    fun List<Post>.filterBy(filter: FeedFilter): List<Post> = when (filter) {
        FeedFilter.SEMUA -> this
        FeedFilter.GRATIS -> filter { it.type == PostType.DONASI }
        FeedFilter.DISKON -> filter { it.type == PostType.FLASH_RESCUE }
    }

    fun List<Post>.sortedByNewest(): List<Post> = sortedByDescending { it.createdAt }
}
