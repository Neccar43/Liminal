package com.novacodestudios.liminal.domain.mapper

import com.novacodestudios.liminal.data.remote.dto.NovelPreviewDto
import com.novacodestudios.liminal.domain.model.NovelPreview

fun NovelPreviewDto.toNovelPreview() = NovelPreview(
    name = name,
    imageUrl = imageUrl,
    detailPageUrl = detailPageUrl,
    source = source,
)

fun NovelPreview.toNovelPreviewDto() = NovelPreviewDto(
    name = name,
    imageUrl = imageUrl,
    detailPageUrl = detailPageUrl,
    source = source,
)

fun List<NovelPreviewDto>.toNovelPreviewList() = map { it.toNovelPreview() }

fun List<NovelPreview>.toNovelPreviewDtoList() = map { it.toNovelPreviewDto() }