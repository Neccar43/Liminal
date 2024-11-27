package com.novacodestudios.liminal.domain.mapper

import com.novacodestudios.liminal.data.remote.dto.NovelDetailDto
import com.novacodestudios.liminal.domain.model.NovelDetail

fun NovelDetailDto.toNovelDetail() = NovelDetail(
    name = name,
    imageUrl = imageUrl,
    author = author,
    summary = summary,
    rate = rate,
    chapters = chapters.map { it.toChapter() },
    source = source,
    status = status,
    tags = tags,
)