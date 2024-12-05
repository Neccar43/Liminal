package com.novacodestudios.liminal.data.mapper

import com.novacodestudios.liminal.data.locale.entity.ChapterEntity
import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.util.createId
import com.novacodestudios.liminal.domain.model.Chapter


fun ChapterDto.toChapter() = Chapter(
    title = title,
    releaseDate = releaseDate,
    url = url,
    filePath = null,
    id = createId(url)
)


fun Chapter.toChapterEntity(seriesId: String, index: Int) = ChapterEntity(
    id = createId(url),
    title = title,
    isRead = false,
    url = url,
    pageIndex = index,
    seriesId = seriesId,
    releaseDate = releaseDate,
    downloadChapterPath = filePath
)


fun ChapterEntity.toChapter() = Chapter(
    title = title,
    releaseDate = releaseDate,
    url = url,
    filePath = downloadChapterPath,
    id = createId(url)
)