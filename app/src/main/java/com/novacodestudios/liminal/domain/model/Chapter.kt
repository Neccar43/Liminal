package com.novacodestudios.liminal.domain.model


data class Chapter(
    val id: String,
    val title: String,
    val releaseDate: String,
    val url: String,
    val filePath: String? = null,
)