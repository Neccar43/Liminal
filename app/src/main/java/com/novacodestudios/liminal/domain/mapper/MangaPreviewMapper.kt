package com.novacodestudios.liminal.domain.mapper

import com.novacodestudios.liminal.data.remote.dto.MangaPreviewDto
import com.novacodestudios.liminal.domain.model.MangaPreview

fun MangaPreviewDto.toMangaPreview() = MangaPreview(
    name = name,
    imageUrl = imageUrl,
    detailPageUrl = detailPageUrl,
    source = source,
)

fun MangaPreview.toMangaPreviewDto() = MangaPreviewDto(
    name = name,
    imageUrl = imageUrl,
    detailPageUrl = detailPageUrl,
    source = source,
)

fun List<MangaPreviewDto>.toMangaPreviewList() = map { it.toMangaPreview() }

fun List<MangaPreview>.toMangaPreviewDtoList() = map { it.toMangaPreviewDto() }