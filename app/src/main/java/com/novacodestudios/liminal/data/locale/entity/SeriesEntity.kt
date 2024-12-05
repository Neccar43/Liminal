package com.novacodestudios.liminal.data.locale.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.domain.model.Source

@Entity
data class SeriesEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val summary: String,
    val lastReadingDateTime: Long,
    val detailPageUrl: String,
    val currentChapterId: String,
    val currentChapterName: String? = null,
    val currentPageIndex: Int,
    val seriesType: SeriesType,
    val author: String,
    val source: Source,
    val status: String,
    val downloadedContentPath: String? = null,
)