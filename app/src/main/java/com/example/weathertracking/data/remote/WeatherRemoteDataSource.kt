package com.example.weathertracking.data.remote

import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.WeatherDataException

/** A place returned by the search endpoint. */
data class RemoteLocation(
    val id: Long,
    val name: String,
    val region: String,
    val country: String,
)

/**
 * The weather backend.
 *
 * - `prod` flavor: `RetrofitWeatherRemoteDataSource` talks to weatherapi.com.
 * - `mock` flavor: `FakeWeatherRemoteDataSource` serves canned data, no API key needed.
 *
 * Implementations must be main-safe and only throw [WeatherDataException].
 */
interface WeatherRemoteDataSource {
    suspend fun searchLocations(query: String): List<RemoteLocation>

    suspend fun currentWeather(locationId: Long): CityWeather
}
