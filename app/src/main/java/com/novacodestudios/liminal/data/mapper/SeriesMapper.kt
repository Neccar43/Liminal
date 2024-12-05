package com.novacodestudios.liminal.data.mapper

import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.data.remote.dto.SeriesDto
import com.novacodestudios.liminal.data.remote.dto.SeriesSummaryDto
import com.novacodestudios.liminal.data.util.createId
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.Series
import com.novacodestudios.liminal.domain.model.SeriesSummary

fun SeriesDto.toSeriesDetail() =
    Series(
        name = name,
        imageUrl = imageUrl,
        summary = summary,
        author = author,
        chapters = chapters.map { it.toChapter() },
        type = seriesType,
        source = source,
        status = status,
        id = createId(detailPageUrl),
        tags = tags,
        detailPageUrl = detailPageUrl,
        currentPageIndex = 0,
        currentChapterName = null,
        lastReadingDateTime = -1,
        currentChapterId = ""
    )

fun SeriesSummaryDto.toModel() =
    SeriesSummary(
        name = name,
        imageUrl = imageUrl,
        detailPageUrl = detailPageUrl,
        source = source,
        type = seriesType
    )


fun SeriesEntity.toSeriesDetail() =
    Series(
        name = name,
        imageUrl = imageUrl,
        summary = summary,
        author = author,
        chapters = emptyList(),
        type = seriesType,
        source = source,
        status = status,
        id = id,
        tags = emptyList(),
        detailPageUrl = detailPageUrl,
        currentPageIndex = currentPageIndex,
        currentChapterName = currentChapterName,
        lastReadingDateTime = lastReadingDateTime,
        currentChapterId = currentChapterId,
    )

fun Series.toSeriesEntity(currentChapter: Chapter) =
    SeriesEntity(
        id = createId(detailPageUrl),
        name = name,
        imageUrl = imageUrl,
        summary = summary,
        lastReadingDateTime = System.currentTimeMillis(),
        detailPageUrl = detailPageUrl,
        currentChapterId = currentChapter.url.hashCode().toString(),
        currentChapterName = currentChapter.title,
        currentPageIndex = currentPageIndex,
        seriesType = type,
        author = author,
        source = source,
        status = status,
        downloadedContentPath = null
    )

