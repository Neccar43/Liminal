package com.novacodestudios.liminal.prensentation.home.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(scrollBehavior: TopAppBarScrollBehavior) {
    CenterAlignedTopAppBar(title = { Text(text = "Liminal") }, scrollBehavior = scrollBehavior)
}