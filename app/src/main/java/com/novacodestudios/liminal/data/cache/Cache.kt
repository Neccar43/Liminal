package com.novacodestudios.liminal.data.cache

import com.novacodestudios.liminal.domain.model.MangaDetail
import com.novacodestudios.liminal.domain.model.MangaPreview
import com.novacodestudios.liminal.domain.model.NovelDetail
import com.novacodestudios.liminal.domain.model.NovelPreview
import com.novacodestudios.liminal.domain.model.SeriesPreview
import io.github.reactivecircus.cache4k.Cache
import kotlin.time.Duration.Companion.days

// TODO: objenin içinde barındır

//val novelDetailCache=Cache.Builder<String, NovelDetail>().build()
//val mangaDetailCache=Cache.Builder<String, MangaDetail>().build()
//
//val novelPreviewCache=Cache.Builder<Unit,List<NovelPreview>>()
//    .expireAfterWrite(1.days)
//    .build()
//
//val mangaPreviewCache=Cache.Builder<Unit,List<MangaPreview>>()
//    .expireAfterWrite(1.days)
//    .build()
