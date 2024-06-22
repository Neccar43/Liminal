package com.novacodestudios.liminal.domain.mapper

import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.domain.model.Chapter


fun ChapterDto.toChapter() = Chapter(
    title = title,
    releaseDate = releaseDate,
    url = url
)

fun Chapter.toChapterDto() = ChapterDto(
    title = title,
    releaseDate = releaseDate,
    url = url
)