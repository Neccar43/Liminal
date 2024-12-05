package com.novacodestudios.liminal.data.util

import android.util.Log
import com.novacodestudios.liminal.domain.model.DataError
import com.novacodestudios.liminal.domain.util.Result
import kotlinx.coroutines.ensureActive
import org.jsoup.HttpStatusException
import java.io.IOException
import java.net.HttpRetryException
import java.net.SocketTimeoutException
import kotlin.coroutines.coroutineContext

suspend fun <T> safeCall(action: suspend () -> T): Result<T, DataError.Network> {
    return try {
        val result = action()
        Result.Success(result)
    } catch (e: IOException) {
        Log.e("safeCall", "Error: $e")
        Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SocketTimeoutException) {
        Log.e("safeCall", "Error: $e")
        Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: HttpRetryException) {
        Log.e("safeCall", "Error: $e")
        Result.Error(DataError.Network.TOO_MANY_REQUESTS)
    } catch (e: HttpStatusException) {
        Log.e("safeCall", "Error: $e")
        when (e.statusCode) {
            401 -> Result.Error(DataError.Network.UNAUTHORIZED)
            409 -> Result.Error(DataError.Network.CONFLICT)
            413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
            in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
            else -> {
                Log.e("safeCall", "Error: $e")
                Result.Error(DataError.Network.UNKNOWN)
            }
        }
    } catch (e: Exception) {
        Log.e("safeCall", "Error: $e")
        coroutineContext.ensureActive()
        Result.Error(DataError.Network.UNKNOWN)
    }
}

private const val TAG = "safeCall"

