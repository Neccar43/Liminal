package com.novacodestudios.liminal.prensentation.navigation

import kotlinx.serialization.Serializable


@Serializable
sealed class Graph {
    @Serializable
    data object Main : Graph()

    @Serializable
    data object Root : Graph()
}
