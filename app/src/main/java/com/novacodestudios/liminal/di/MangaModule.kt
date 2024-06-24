package com.novacodestudios.liminal.di

import android.content.Context
import androidx.room.Room
import com.novacodestudios.liminal.data.locale.ChapterDao
import com.novacodestudios.liminal.data.locale.LiminalDatabase
import com.novacodestudios.liminal.data.locale.SeriesDao
import com.novacodestudios.liminal.data.remote.SadScansScrapper
import com.novacodestudios.liminal.data.remote.TempestScrapper
import com.novacodestudios.liminal.data.remote.TurkceLightNovelScrapper
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.MangaRepository
import com.novacodestudios.liminal.data.repository.NovelRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MangaModule {

    @Singleton
    @Provides
    fun provideMangaRepository(
        tempestScrapper: TempestScrapper,
        sadScansScrapper: SadScansScrapper
    ): MangaRepository {
        return MangaRepository(tempestScrapper, sadScansScrapper)
    }

    @Singleton
    @Provides
    fun provideNovelRepository(
        turkceLightNovelScrapper: TurkceLightNovelScrapper
    ): NovelRepository {
        return NovelRepository(turkceLightNovelScrapper)
    }

    @Singleton
    @Provides
    fun provideTempestScrapper(@ApplicationContext context: Context): TempestScrapper {
        return TempestScrapper(context)
    }

    @Singleton
    @Provides
    fun provideSadScansScrapper(@ApplicationContext context: Context): SadScansScrapper {
        return SadScansScrapper(context)
    }

    @Singleton
    @Provides
    fun provideTurkceLightNovelScrapper(@ApplicationContext context: Context): TurkceLightNovelScrapper {
        return TurkceLightNovelScrapper(context)
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): LiminalDatabase =
        Room.databaseBuilder(context, klass = LiminalDatabase::class.java, name = "liminal_db")
            .build()


    @Singleton
    @Provides
    fun provideChapterDao(database: LiminalDatabase) = database.chapterDao()

    @Singleton
    @Provides
    fun provideSeriesDao(database: LiminalDatabase) = database.seriesDao()

    @Singleton
    @Provides
    fun provideChapterRepository(chapterDao: ChapterDao) = ChapterRepository(chapterDao)

    @Singleton
    @Provides
    fun provideSeriesRepository(seriesDao: SeriesDao) = SeriesRepository(seriesDao)


}