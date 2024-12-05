package com.novacodestudios.liminal.domain.model

// TODO: Ui ile ilgili olan alanları kaldırıp başka bir modele taşı
data class Series(
    val id: String,
    val name: String,
    val imageUrl: String,
    val summary: String,
    val author: String,
    val chapters: List<Chapter>,
    val type: SeriesType,
    val source: Source,
    val status: String,
    val tags: List<Tag>,
    val detailPageUrl: String,
    val currentPageIndex: Int,
    val currentChapterName: String?,
    val currentChapterId: String,
    val lastReadingDateTime: Long,

    )
