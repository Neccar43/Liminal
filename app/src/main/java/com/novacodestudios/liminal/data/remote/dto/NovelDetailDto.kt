package com.novacodestudios.liminal.data.remote.dto

data class NovelDetailDto(
    val name: String,
    val imageUrl: String,
    val author: String,
    //val status: String, // TODO: Enuma çevir
    //val releaseDate: String,
    val rate: String,
    val summary: String,
    val chapters: List<ChapterDto>
)