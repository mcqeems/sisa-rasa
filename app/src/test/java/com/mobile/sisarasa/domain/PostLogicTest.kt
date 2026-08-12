package com.mobile.sisarasa.domain

import com.mobile.sisarasa.domain.PostLogic.canBeClaimedBy
import com.mobile.sisarasa.domain.PostLogic.canBeManagedBy
import com.mobile.sisarasa.domain.PostLogic.claim
import com.mobile.sisarasa.domain.PostLogic.filterBy
import com.mobile.sisarasa.domain.model.FeedFilter
import com.mobile.sisarasa.domain.model.Location
import com.mobile.sisarasa.domain.model.Post
import com.mobile.sisarasa.domain.model.PostStatus
import com.mobile.sisarasa.domain.model.PostType
import com.mobile.sisarasa.ui.components.discountPercent
import com.mobile.sisarasa.ui.components.priceLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostLogicTest {

    private val location = Location("Jl. Contoh 1", -7.0, 110.0)

    private fun post(
        id: String = "p",
        type: PostType = PostType.DONASI,
        status: PostStatus = PostStatus.AVAILABLE,
        donorId: String = "donor",
        claimerId: String? = null,
    ) = Post(
        id = id,
        type = type,
        title = "Postingan",
        status = status,
        location = location,
        donorId = donorId,
        donorName = "Donatur",
        claimerId = claimerId,
        createdAt = 1L,
    )

    @Test
    fun `available post claimable by others but not owner`() {
        val p = post()
        assertTrue(p.canBeClaimedBy("other"))
        assertFalse(p.canBeClaimedBy(p.donorId))
    }

    @Test
    fun `non available post not claimable`() {
        assertFalse(post(status = PostStatus.CLAIMED).canBeClaimedBy("other"))
        assertFalse(post(status = PostStatus.SOLD_OUT).canBeClaimedBy("other"))
    }

    @Test
    fun `claim only succeeds then no longer available`() {
        val claimed = post().claim("other")
        assertEquals(PostStatus.CLAIMED, claimed.status)
        assertEquals("other", claimed.claimerId)
        assertFalse(claimed.canBeClaimedBy("another"))
    }

    @Test
    fun `owner cannot edit or delete claimed post`() {
        val claimed = post(status = PostStatus.CLAIMED)
        assertFalse(claimed.canBeManagedBy(claimed.donorId))
        assertTrue(post().canBeManagedBy("donor"))
    }

    @Test
    fun `feed filter splits donasi and flash rescue`() {
        val posts = listOf(
            post(id = "a", type = PostType.DONASI),
            post(id = "b", type = PostType.FLASH_RESCUE),
            post(id = "c", type = PostType.FLASH_RESCUE),
        )
        assertEquals(listOf("a"), posts.filterBy(FeedFilter.GRATIS).map { it.id })
        assertEquals(listOf("b", "c"), posts.filterBy(FeedFilter.DISKON).map { it.id })
        assertEquals(3, posts.filterBy(FeedFilter.SEMUA).size)
    }

    @Test
    fun `price label shows gratis for donasi and diskon percentage for flash`() {
        val donasi = post()
        assertEquals("Gratis", donasi.priceLabel())
        val flash = post(type = PostType.FLASH_RESCUE)
            .copy(price = 20_000, originalPrice = 50_000)
        assertEquals(60, flash.discountPercent())
    }
}