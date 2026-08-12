package com.mobile.sisarasa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mobile.sisarasa.domain.model.Post
import com.mobile.sisarasa.domain.model.PostStatus
import com.mobile.sisarasa.domain.model.PostType
import java.util.Locale

fun Post.statusLabel(): String = when (status) {
    PostStatus.AVAILABLE -> "Tersedia"
    PostStatus.CLAIMED -> "Diklaim"
    PostStatus.SOLD_OUT -> "Habis Terjual"
}

fun Post.priceLabel(): String = when {
    type == PostType.DONASI -> "Gratis"
    originalPrice != null && originalPrice > price -> "Rp ${price.formatRp()}  (diskon ${discountPercent()}%)"
    else -> "Rp ${price.formatRp()}"
}

fun Long.formatRp(): String = String.format(Locale.getDefault(), "%,d", this).replace(',', '.')

fun Post.discountPercent(): Int = when {
    originalPrice != null && originalPrice > 0 && originalPrice > price ->
        ((originalPrice - price) * 100 / originalPrice).toInt()
    else -> 0
}

@Composable
fun StatusChip(post: Post) {
    val (bg, fg) = when (post.status) {
        PostStatus.AVAILABLE -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        PostStatus.CLAIMED -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        PostStatus.SOLD_OUT -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = post.statusLabel(),
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
