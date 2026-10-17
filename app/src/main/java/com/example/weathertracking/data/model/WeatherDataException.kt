package com.example.weathertracking.data.model

/** Why a call to the weather service failed, in terms the rest of the app can act on. */
enum class FailureReason {
    /** No connection, timeout, DNS failure… Retrying later may help. */
    Network,

    /** The service rejected the API key (prod flavor without a valid WEATHER_API_KEY). */
    InvalidApiKey,

    /** Anything else: unexpected status code or payload. */
    Unknown,
}

/** The only exception the data layer lets escape. Callers switch on [reason]. */
class WeatherDataException(
    val reason: FailureReason,
    cause: Throwable? = null,
) : Exception("Weather service failure: $reason", cause)
