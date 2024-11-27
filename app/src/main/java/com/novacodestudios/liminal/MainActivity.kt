package com.novacodestudios.liminal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.novacodestudios.liminal.prensentation.component.BottomBar
import com.novacodestudios.liminal.prensentation.detail.DetailScreen
import com.novacodestudios.liminal.prensentation.mangaReading.MangaReadingScreen
import com.novacodestudios.liminal.prensentation.navigation.CustomNavType
import com.novacodestudios.liminal.prensentation.navigation.Graph
import com.novacodestudios.liminal.prensentation.navigation.NavArguments
import com.novacodestudios.liminal.prensentation.navigation.Screen
import com.novacodestudios.liminal.prensentation.navigation.UiChapter
import com.novacodestudios.liminal.prensentation.navigation.mainGraph
import com.novacodestudios.liminal.prensentation.navigation.toUiChapter
import com.novacodestudios.liminal.prensentation.novelReading.NovelReadingScreen
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import com.novacodestudios.liminal.util.encodeUrl
import com.novacodestudios.liminal.util.withEncodedUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import kotlin.reflect.typeOf

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }


        enableEdgeToEdge()
        setContent {
            LiminalTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold(
                        bottomBar = {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination?.parent
                            val isMainGraph =
                                currentDestination?.hierarchy?.any { it.hasRoute(Graph.Main::class) } == true
                            val isSelected =
                                navBackStackEntry?.destination?.hasRoute(Screen.Home::class)
                            LaunchedEffect(isSelected) {
                                Log.d(TAG, "onCreate: isSelected: $isSelected")
                            }

                            if (isMainGraph) {
                                BottomBar(navController = navController)
                            }

                        },
                        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
                            .exclude(TopAppBarDefaults.windowInsets)
                    ) { paddingValues ->
                        NavHost(
                            modifier = Modifier.padding(paddingValues),
                            navController = navController,
                            startDestination = Graph.Main,
                            route = Graph.Root::class
                        ) {
                            mainGraph(navController)

                            composable<Screen.Detail> {
                                DetailScreen(
                                    onNavigateUp = { navController.navigateUp() },
                                    onNavigateMangaReadingScreen = { chapter, chapters ->
                                        Log.d(TAG, "onNavigateMangaReadingScreen: çalıştı $chapter")
                                        NavArguments.currentChapter = chapter
                                        NavArguments.chapterList = chapters

                                        navController.navigate(
                                            Screen.MangaReading
                                        )
                                    },
                                    onNavigateNovelReadingScreen = { chapter, detailUrl ->
                                        NavArguments.currentChapter = chapter
                                        navController.navigate(
                                            Screen.NovelReading
                                        )
                                    },
                                )
                            }

                            composable<Screen.MangaReading>(
                                typeMap = mapOf(
                                    typeOf<UiChapter>() to CustomNavType.UiChapterType,
                                )
                            ) {
                                MangaReadingScreen(onNavigateUp = {
                                    navController.navigateUp()
                                })
                            }
                            composable<Screen.NovelReading>(
                                typeMap = mapOf(
                                    typeOf<UiChapter>() to CustomNavType.UiChapterType
                                )
                            ) {
                                NovelReadingScreen(onNavigateUp = {
                                    navController.navigateUp()
                                })
                            }
                        }


                    }
                }
            }
        }
    }


    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun RequestPermissionsScreen(modifier: Modifier) {
    val context = LocalContext.current

    // İzni başlatıcı (Launcher)
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Sonuçların değerlendirilmesi
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value
            if (isGranted) {
                Log.d("PermissionRequest", "$permission granted.")
            } else {
                Log.d("PermissionRequest", "$permission denied.")
                // Gerekirse kullanıcıya izin vermesi gerektiğini bildirebilirsin
            }
        }
    }

    Column(modifier = modifier) {
        Button(onClick = {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.WAKE_LOCK
                )
            )
        }) {
            Text("İzinleri İste")
        }
    }
}

fun requestManageExternalStoragePermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:" + context.packageName)
        context.startActivity(intent)
    }
}

