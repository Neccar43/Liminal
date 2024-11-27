package com.novacodestudios.liminal.data.remote.dto

import com.novacodestudios.liminal.domain.model.Source

data class MangaDetailDto(
    val name: String,
    val imageUrl: String,
    val summary: String,
    val author: String,
    val chapters: List<ChapterDto>,
    val status: String,
    val tags: List<Tag>,
    val source: Source,
    )

// TODO: Uygun bir yere taşı
data class Tag(val name: String, val url: String)