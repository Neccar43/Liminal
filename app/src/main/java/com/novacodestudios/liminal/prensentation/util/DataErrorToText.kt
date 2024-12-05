package com.novacodestudios.liminal.prensentation.util

import com.novacodestudios.liminal.R
import com.novacodestudios.liminal.domain.model.DataError

fun DataError.toUiText(): UiText {
    val stringResource = when (this) {
        DataError.Network.REQUEST_TIMEOUT -> R.string.error_request_timeout
        DataError.Network.UNAUTHORIZED -> R.string.error_unauthorized
        DataError.Network.CONFLICT -> R.string.error_conflict
        DataError.Network.TOO_MANY_REQUESTS -> R.string.error_too_many_requests
        DataError.Network.NO_INTERNET -> R.string.error_no_internet
        DataError.Network.PAYLOAD_TOO_LARGE -> R.string.error_payload_too_large
        DataError.Network.SERVER_ERROR -> R.string.error_server_error
        DataError.Network.SERIALIZATION -> R.string.error_serialization
        DataError.Network.UNKNOWN -> R.string.error_unknown
        DataError.Local.DISK_FULL -> R.string.error_disk_full
    }

    return UiText.StringResource(stringResource)
}
