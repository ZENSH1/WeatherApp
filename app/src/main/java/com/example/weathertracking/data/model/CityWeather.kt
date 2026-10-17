package com.example.weathertracking.data.model

/**
 * The current weather in one city.
 *
 * This is the type the whole app speaks. The network DTOs and the Room entity are mapped to it,
 * so neither of them leaks out of the data layer.
 */
data class CityWeather(
    /** Stable location id from the weather service. */
    val id: Long,
    val name: String,
    val region: String,
    val country: String,
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val condition: String,
    val iconUrl: String,
    val humidityPercent: Int,
    val uvIndex: Double,
    /** When the weather service last updated this reading, in seconds since the epoch. */
    val lastUpdatedEpochSeconds: Long,
) {
    /** "Region, Country" without blank or repeated parts, e.g. "Sindh, Pakistan". */
    val subtitle: String
        get() = listOf(region, country).filter { it.isNotBlank() && it != name }.distinct().joinToString(", ")
}
