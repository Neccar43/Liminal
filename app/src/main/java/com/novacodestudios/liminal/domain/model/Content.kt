package com.novacodestudios.liminal.domain.model

sealed class Content {
    data class Image(val url: String) : Content()
    data class Text(val content: String) : Content()
}
