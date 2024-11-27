package com.novacodestudios.liminal.domain.model

import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.data.remote.dto.Tag
import com.novacodestudios.liminal.util.hashToMD5
import kotlinx.serialization.Serializable


// TODO: Bu sınıfı data classa çevir
open class SeriesDetail(
    open val name: String,
    open val imageUrl: String,
    open val author: String,
    open val summary: String,
    open val chapters: List<Chapter>,
    open val type: SeriesType,
    open val source: Source,
    open val status:String,
    open val tags:List<Tag>
) {
    // TODO: Rename
    fun copy2(
        name: String = this.name,
        imageUrl: String = this.imageUrl,
        author: String = this.author,
        summary: String = this.summary,
        chapters: List<Chapter> = this.chapters,
        type: SeriesType = this.type,
        source: Source = this.source,
        status: String = this.status,
        tags: List<Tag> = this.tags
    ): SeriesDetail {
        return SeriesDetail(
            name = name,
            imageUrl = imageUrl,
            author = author,
            summary = summary,
            chapters = chapters,
            type = type,
            source = source,
            status = status,
            tags = tags
        )
    }
}

fun SeriesDetail.toSeriesEntity(detailPageUrl: String): SeriesEntity {
    return SeriesEntity(
        id = detailPageUrl.hashToMD5(),
        name = name,
        imageUrl = imageUrl,
        lastReadingDateTime = System.currentTimeMillis(),
        detailPageUrl = detailPageUrl,
        currentChapterId = chapters.first().url.hashToMD5(),
        currentPageIndex = 0,
        isManga = type == SeriesType.MANGA
    )
}