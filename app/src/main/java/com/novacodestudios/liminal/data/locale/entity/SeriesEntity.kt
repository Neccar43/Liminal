package com.novacodestudios.liminal.data.locale.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SeriesEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val lastReadingDateTime: Long,
    val detailPageUrl: String,
    val currentChapterId: String,
    val currentPageIndex: Int,
    val isManga: Boolean
)