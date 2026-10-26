package com.miroshnikov.messenger.data

import kotlinx.serialization.json.Json

val chatJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
