package com.novacodestudios.liminal.domain.mapper

import com.novacodestudios.liminal.data.remote.dto.MangaDetailDto
import com.novacodestudios.liminal.domain.model.MangaDetail

fun MangaDetailDto.toMangaDetail() = MangaDetail(
    name = name,
    imageUrl = imageUrl,
    summary = summary,
    author = author,
    chapters = chapters.map { it.toChapter() }
)

fun MangaDetail.toMangaDetailDto() = MangaDetailDto(
    name = name,
    imageUrl = imageUrl,
    summary = summary,
    author = author,
    chapters = chapters.map { it.toChapterDto() },
    )