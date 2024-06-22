package com.novacodestudios.liminal.data.remote.dto

data class MangaDetailDto(
    val name: String,
    val imageUrl: String,
    val summary: String,
    val author: String,
    //val status: Status,
    val chapters: List<ChapterDto>,
)