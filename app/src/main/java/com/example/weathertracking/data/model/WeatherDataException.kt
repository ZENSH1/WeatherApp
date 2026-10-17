package com.example.weathertracking.data.model

/** Why a weather call failed. */
enum class FailureReason { Network, InvalidApiKey, Unknown }

/** The only exception the data layer throws. */
class WeatherDataException(
    val reason: FailureReason,
    cause: Throwable? = null,
) : Exception("Weather service failure: $reason", cause)
