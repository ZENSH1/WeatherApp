package com.example.weathertracking.data.model

/** The current weather in one city. Network DTOs and the Room entity are mapped to this. */
data class CityWeather(
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
    val lastUpdatedEpochSeconds: Long,
) {
    /** e.g. "Sindh, Pakistan", without blank or repeated parts. */
    val subtitle: String
        get() = listOf(region, country).filter { it.isNotBlank() && it != name }.distinct().joinToString(", ")
}
