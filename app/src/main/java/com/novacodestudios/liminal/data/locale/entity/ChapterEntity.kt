package com.novacodestudios.liminal.data.locale.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = SeriesEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("seriesId"),
        onDelete = ForeignKey.CASCADE,
    )]
)
data class ChapterEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isRead: Boolean,
    val url: String,
    val pageIndex: Int,
    val seriesId: String,
    val releaseDate: String,
    val downloadChapterPath:String?=null
)