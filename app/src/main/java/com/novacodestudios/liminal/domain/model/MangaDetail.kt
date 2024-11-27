package com.novacodestudios.liminal.domain.model

import com.novacodestudios.liminal.data.remote.dto.Tag


data class MangaDetail(
    override val name: String,
    override val imageUrl: String,
    override val summary: String,
    override val author: String,
    override val chapters: List<Chapter>,
    override val type: SeriesType = SeriesType.MANGA,
    override val source: Source,
    override val status:String,
    override val tags:List<Tag>
) : SeriesDetail(name, imageUrl, author, summary, chapters, type,source, status, tags)