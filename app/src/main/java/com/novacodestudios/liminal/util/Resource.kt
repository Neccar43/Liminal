package com.novacodestudios.liminal.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val exception: Exception) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()

    companion object {
        fun <T> success(data: T): Resource<T> = Success(data)
        fun error(exception: Exception): Resource<Nothing> = Error(exception)
        fun loading(): Resource<Nothing> = Loading
    }
}

/*inline fun <T> runSafely(block: () -> T): Resource<T> {
    return try {
        Resource.success(block())
    } catch (e: Exception) {
        Resource.error(e)
    }
}*/

fun <T> executeWithResource(
    errorLog: (Exception) -> Unit = {},
    block: suspend () -> T
): Flow<Resource<T>> {
    return flow {
        emit(Resource.loading())
        try {
            emit(Resource.success(block()))
        } catch (e: Exception) {
            errorLog(e)
            emit(Resource.error(e))
        }
    }
}


fun <T> executeWithResourceFlow(block: suspend () -> Flow<T>): Flow<Resource<T>> = channelFlow {
    send(Resource.Loading)
    try {
        block()
            .collectLatest {
                send(Resource.Success(it))
            }
    } catch (e: Exception) {
        send(Resource.Error(e))
    }
}

fun <T> Flow<T>.asResource(): Flow<Resource<T>> {
    return this
        .map { value ->
            Resource.success(value)
        }
        .onStart {
            emit(Resource.loading())
        }
        .catch { e ->
            emit(Resource.error(Exception(e)))
        }
}