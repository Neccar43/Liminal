package com.novacodestudios.liminal.domain.model

data class RecentMangaChapters(
    val name: String,
    val imageUrl: String,
    val chapters: List<Chapter>
)