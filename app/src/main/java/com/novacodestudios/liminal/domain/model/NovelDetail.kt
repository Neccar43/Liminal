package com.novacodestudios.liminal.domain.model

import com.novacodestudios.liminal.data.remote.dto.Tag

data class NovelDetail(
    override val name: String,
    override val imageUrl: String,
    override val author: String,
    val rate: String,
    override val summary: String,
    override val chapters: List<Chapter>,
    override val type: SeriesType = SeriesType.NOVEL,
    override val source: Source,
    override val status: String,
    override val tags: List<Tag>,
    ) : SeriesDetail(name, imageUrl, author, summary, chapters, type, source, status, tags)