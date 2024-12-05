package com.novacodestudios.liminal.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.novacodestudios.liminal.data.locale.ChapterDao
import com.novacodestudios.liminal.data.locale.LiminalDatabase
import com.novacodestudios.liminal.data.locale.SeriesDao
import com.novacodestudios.liminal.data.remote.SadScansScraper
import com.novacodestudios.liminal.data.remote.TempestScraper
import com.novacodestudios.liminal.data.remote.TurkceLightNovelScraper
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Singleton
    @Provides
    fun provideTempestScrapper(): TempestScraper {
        return TempestScraper()
    }

    @Singleton
    @Provides
    fun provideSadScansScrapper(): SadScansScraper {
        return SadScansScraper()
    }

    @Singleton
    @Provides
    fun provideTurkceLightNovelScrapper(): TurkceLightNovelScraper {
        return TurkceLightNovelScraper()
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
    fun provideSeriesRepository(
        tempestScraper: TempestScraper,
        sadScansScraper: SadScansScraper,
        novelScraper: TurkceLightNovelScraper,
        seriesDao: SeriesDao
    ) = SeriesRepository(
        scrapers = listOf(tempestScraper, sadScansScraper, novelScraper),
        seriesDao
    )

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext appContext: Context): WorkManager =
        WorkManager.getInstance(appContext)


    @Provides
    @Singleton
    fun injectFileDir(@ApplicationContext context: Context): File {
        return context.filesDir
    }

}