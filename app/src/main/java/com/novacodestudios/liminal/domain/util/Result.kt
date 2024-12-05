package com.novacodestudios.liminal.domain.util

import android.util.Log
import com.novacodestudios.liminal.domain.model.DataError
import kotlinx.coroutines.delay

typealias RootError = com.novacodestudios.liminal.domain.model.Error

sealed interface Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>
}

inline fun <T, E : RootError, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

fun <T, E : RootError> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return map { }
}

inline fun <T, E : RootError> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E : RootError> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> {
            action(error)
            this
        }

        is Result.Success -> this
    }
}

suspend fun <T> retryWithPolicy(
    maxAttempts: Int = 3,
    delayDuration: Long = 1000L,
    block: suspend () -> Result<T, DataError.Network>
): Result<T, DataError.Network> {
    var currentAttempt = 0
    var lastError: DataError.Network? = null
    val errorList = listOf(DataError.Network.REQUEST_TIMEOUT, DataError.Network.NO_INTERNET)

    while (currentAttempt < maxAttempts) {
        when (val result = block()) {
            is Result.Error -> {
                lastError = result.error
                Log.d("Retry", "Attempt ${currentAttempt + 1} failed: $lastError")
                if (!errorList.contains(result.error)) break
            }

            is Result.Success -> return result
        }

        currentAttempt++
        delay(delayDuration)
    }

    return Result.Error(lastError ?: DataError.Network.UNKNOWN)
}


typealias EmptyResult<E> = Result<Unit, E>