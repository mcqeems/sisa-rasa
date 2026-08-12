package com.mobile.sisarasa.domain.model

enum class UserRole { PERSONAL, MITRA_BISNIS }

enum class PostType { DONASI, FLASH_RESCUE }

enum class PostStatus { AVAILABLE, CLAIMED, SOLD_OUT }

data class Location(
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

data class Post(
    val id: String,
    val type: PostType,
    val title: String,
    val description: String = "",
    val photoUrl: String? = null,
    val price: Long = 0L,
    val originalPrice: Long? = null,
    val status: PostStatus = PostStatus.AVAILABLE,
    val location: Location,
    val donorId: String,
    val donorName: String,
    val claimerId: String? = null,
    val createdAt: Long,
)

data class AppUser(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
)

enum class FeedFilter(val label: String) {
    SEMUA("Semua"),
    GRATIS("Gratis"),
    DISKON("Diskon"),
}
