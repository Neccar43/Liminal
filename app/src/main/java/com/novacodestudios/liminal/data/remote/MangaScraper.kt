package com.novacodestudios.liminal.data.remote

import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.MangaDetailDto
import com.novacodestudios.liminal.data.remote.dto.MangaPreviewDto

interface MangaScraper {
    val baseUrl: String
    suspend fun getMangaList(pageNumber: Int = 1): List<MangaPreviewDto>
    suspend fun getMangaChapterList(detailPageUrl: String): List<ChapterDto>
    suspend fun getMangaDetail(detailPageUrl: String): MangaDetailDto
    suspend fun getMangaChapterImages(chapterUrl: String): List<String>
}

// TODO: Manga tr den de scrape et

//https://mangatr.net/