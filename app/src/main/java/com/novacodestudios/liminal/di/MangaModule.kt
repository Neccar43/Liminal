package com.novacodestudios.liminal.di

import android.content.Context
import com.novacodestudios.liminal.data.remote.SadScansScrapper
import com.novacodestudios.liminal.data.remote.TempestScrapper
import com.novacodestudios.liminal.data.remote.TurkceLightNovelScrapper
import com.novacodestudios.liminal.data.repository.MangaRepository
import com.novacodestudios.liminal.data.repository.NovelRepository
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


}