package com.novacodestudios.liminal.data.remote.dto

import com.novacodestudios.liminal.domain.model.Source

data class NovelDetailDto(
    val name: String,
    val imageUrl: String,
    val author: String,
    val status: String,
    val rate: String,
    val summary: String,
    val chapters: List<ChapterDto>,
    val tags: List<Tag>,
    val source: Source,
)