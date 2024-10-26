package com.novacodestudios.liminal.data.repository

import android.content.Context
import com.ketch.DownloadModel
import com.ketch.Ketch
import kotlinx.coroutines.flow.StateFlow

class DownloadRepository(private val context: Context) {

    fun downloadSeries(filePath:String,url:String): StateFlow<List<DownloadModel>> {
        val ketch=Ketch.init(context)
        val request= ketch.download(
            url = url,
            path = filePath,
            onQueue = {},
            onStart = { length -> },
            onProgress = { progress, speedInBytePerM-> },
            onSuccess = { },
            onFailure = { error -> },
            onCancel = { }
        )

        return ketch.observeDownloads()
    }
}