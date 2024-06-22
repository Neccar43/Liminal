package com.novacodestudios.liminal.data.remote.dto

data class RecentMangaChaptersDto(
    val name: String,
    val imageUrl: String,
    val chapters: List<ChapterDto>
)