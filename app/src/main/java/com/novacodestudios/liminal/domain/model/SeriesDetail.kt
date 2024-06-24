package com.novacodestudios.liminal.domain.model

import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.util.hashToMD5
import kotlinx.serialization.Serializable

@Serializable
open class SeriesDetail (
    open val name: String,
    open val imageUrl: String,
    open val author: String,
    open val summary: String,
    open val chapters: List<Chapter>,
    open val type:SeriesType,
)

fun SeriesDetail.toSeriesEntity(detailPageUrl:String): SeriesEntity {
    return SeriesEntity(
        id = detailPageUrl.hashToMD5(),
        name = name,
        imageUrl = imageUrl,
        //author = author,
       // summary = summary,
        lastReadingDateTime = System.currentTimeMillis(),
        detailPageUrl = detailPageUrl,
        currentChapterId = chapters.first().url.hashToMD5(),
        currentPageIndex = 0,
        isManga = type == SeriesType.MANGA
    )
}