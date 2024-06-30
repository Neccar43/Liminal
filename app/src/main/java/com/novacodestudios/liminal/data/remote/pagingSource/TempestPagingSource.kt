package com.novacodestudios.liminal.data.remote.pagingSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.novacodestudios.liminal.data.remote.TempestScrapper
import com.novacodestudios.liminal.data.remote.dto.MangaPreviewDto
import com.novacodestudios.liminal.domain.mapper.toMangaPreviewList
import com.novacodestudios.liminal.domain.model.MangaPreview
import javax.inject.Inject

class TempestPagingSource(
    private val scraper:TempestScrapper,
) : PagingSource<Int, MangaPreview>() {
    override fun getRefreshKey(state: PagingState<Int, MangaPreview>): Int? {
        return state.anchorPosition?.let {anchorPosition->
            val anchorPage= state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1)?:anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MangaPreview> {
        return try {
            val page = params.key ?: 1
            val previewList=scraper.getMangaList(pageNumber = page).toMangaPreviewList()

            LoadResult.Page(
                data = previewList,
                prevKey = if (page==1) null else page.minus(1),
                nextKey = if (previewList.isEmpty()) null else page.plus(1),
            )

        } catch (e: Exception) {
            LoadResult.Error(throwable = e)
        }
    }
}