package com.mobile.sisarasa.data.local

import com.mobile.sisarasa.domain.model.Location
import com.mobile.sisarasa.domain.model.Post
import com.mobile.sisarasa.domain.model.PostType

// ponytail: in-memory seed so the app demos with zero backend.
object SampleData {
    val donaturId = "user-personal"
    val mitraId = "user-mitra"

    val posts = listOf(
        Post(
            id = "p1",
            type = PostType.DONASI,
            title = "Nasi Kotak Sisa Hajatan",
            description = "12 nasi kotak lengkap lauk ayam, masih bersih dan layak konsumsi. Gratis untuk yang membutuhkan.",
            photoUrl = "https://picsum.photos/seed/nasikotak/600/400",
            price = 0L,
            originalPrice = null,
            location = Location("Jl. Melati No. 10, Yogyakarta", -7.7956, 110.3695),
            donorId = donaturId,
            donorName = "Budi Santoso",
            createdAt = 1_756_000_000_000L,
        ),
        Post(
            id = "p2",
            type = PostType.DONASI,
            title = "Roti Tawar Fresh Panjang Umur",
            description = "2 kemasan roti tawar masih segar, sayang dibuang. Bisa dijemput sore ini.",
            photoUrl = "https://picsum.photos/seed/roti/600/400",
            price = 0L,
            originalPrice = null,
            location = Location("Kost Putri Mawar, Jl. Kaliurang Km 5", -7.7603, 110.4027),
            donorId = donaturId,
            donorName = "Budi Santoso",
            createdAt = 1_755_990_000_000L,
        ),
        Post(
            id = "p3",
            type = PostType.FLASH_RESCUE,
            title = "Ayam Goreng Paket Hemat",
            description = "Sisa produksi hari ini, diskon 60%. Ambil sebelum toko tutup 22.00.",
            photoUrl = "https://picsum.photos/seed/ayam/600/400",
            price = 20_000L,
            originalPrice = 50_000L,
            location = Location("Resto Ayam Bakar Mas Joko, Jl. Gejayan No. 22", -7.7718, 110.3811),
            donorId = mitraId,
            donorName = "Resto Mas Joko",
            createdAt = 1_755_980_000_000L,
        ),
        Post(
            id = "p4",
            type = PostType.FLASH_RESCUE,
            title = "Pastry Surplus - Diskon 70%",
            description = "Croissant dan donat sisa hari ini. Diambil sebelum jam 19.00.",
            photoUrl = "https://picsum.photos/seed/pastry/600/400",
            price = 15_000L,
            originalPrice = 50_000L,
            location = Location("Cafe Kopi Senja, Jl. Affandi No. 5", -7.7898, 110.4057),
            donorId = mitraId,
            donorName = "Cafe Kopi Senja",
            createdAt = 1_755_970_000_000L,
        ),
        Post(
            id = "p5",
            type = PostType.DONASI,
            title = "Lauk Sayur Sop Masih Hangat",
            description = "Sayur sop panci sedang, sisa acara kantor. Gratis untuk siapapun.",
            photoUrl = "https://picsum.photos/seed/sop/600/400",
            price = 0L,
            originalPrice = null,
            location = Location("Kantor RW, Jl. Bantul No. 3", -7.8113, 110.3646),
            donorId = donaturId,
            donorName = "Budi Santoso",
            createdAt = 1_755_960_000_000L,
        ),
        Post(
            id = "p6",
            type = PostType.FLASH_RESCUE,
            title = "Nasi Goreng Spesial Diskon",
            description = "Nasi goreng spesial sisa orderan, masih hangat. Diskon 55% sebelum tutup.",
            photoUrl = "https://picsum.photos/seed/nasgor/600/400",
            price = 18_000L,
            originalPrice = 40_000L,
            location = Location("Warung Sederhana, Jl. Kaliurang No. 15", -7.7721, 110.4042),
            donorId = mitraId,
            donorName = "Warung Sederhana",
            createdAt = 1_755_950_000_000L,
        ),
    )
}
