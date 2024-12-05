package com.novacodestudios.liminal.data.locale

import androidx.room.Database
import androidx.room.RoomDatabase
import com.novacodestudios.liminal.data.locale.entity.ChapterEntity
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity

@Database(entities = [SeriesEntity::class, ChapterEntity::class], version = 5)
abstract class LiminalDatabase : RoomDatabase() {
    abstract fun seriesDao(): SeriesDao
    abstract fun chapterDao(): ChapterDao
}