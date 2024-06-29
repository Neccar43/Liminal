package com.novacodestudios.liminal.domain.mapper

import com.novacodestudios.liminal.data.locale.entity.ChapterEntity
import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.util.hashToMD5


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

fun Chapter.toChapterEntity(seriesId: String, index: Int) = ChapterEntity(
    id = url.hashToMD5(),
    title = title,
    isRead = false,
    //releaseDate = releaseDate,
    url = url,
    pageIndex = index,
    seriesId = seriesId,
    releaseDate = releaseDate
)

fun List<Chapter>.toChapterEntityList(seriesId: String) = mapIndexed { index, chapter ->
    chapter.toChapterEntity(seriesId, index)
}

fun ChapterEntity.toChapter() = Chapter(
    title = title,
    releaseDate = releaseDate,
    url = url
)

fun List<ChapterEntity>.toChapterList() = map { it.toChapter() }