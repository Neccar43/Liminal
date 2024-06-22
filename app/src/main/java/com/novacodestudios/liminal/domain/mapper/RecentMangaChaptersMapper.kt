package com.novacodestudios.liminal.domain.mapper

import com.novacodestudios.liminal.data.remote.dto.RecentMangaChaptersDto
import com.novacodestudios.liminal.domain.model.RecentMangaChapters

fun RecentMangaChaptersDto.toRecentMangaChapters() = RecentMangaChapters(
    name = name,
    imageUrl = imageUrl,
    chapters = chapters.map { it.toChapter() }
)

fun RecentMangaChapters.toRecentMangaChaptersDto() = RecentMangaChaptersDto(
    name = name,
    imageUrl = imageUrl,
    chapters = chapters.map { it.toChapterDto() }
)