package com.novacodestudios.liminal.prensentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.novacodestudios.liminal.prensentation.navigation.BottomNavigationItem
import com.novacodestudios.liminal.prensentation.navigation.Screen

@Composable
fun BottomBar(navController: NavHostController) {
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    val navItems=listOf(
        BottomNavigationItem(
            title = "Ana Sayfa",
            selectedIcon = Icons.Filled.Home,
            unSelectedIcon = Icons.Outlined.Home,
            route = Screen.Home
        ),
        BottomNavigationItem(
            title = "Kütüphane",
            selectedIcon = Icons.Filled.CollectionsBookmark,
            unSelectedIcon = Icons.Outlined.CollectionsBookmark,
            route = Screen.Library
        ),
    )
    NavigationBar {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                label = { Text(text = item.title) },
                alwaysShowLabel = false,
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selectedItemIndex == index) {
                            item.selectedIcon
                        } else {
                            item.unSelectedIcon
                        },
                        contentDescription = item.title
                    )
                })
        }
    }
}

