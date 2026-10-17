package com.example.weathertracking.data.remote

import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.WeatherDataException

data class RemoteLocation(
    val id: Long,
    val name: String,
    val region: String,
    val country: String,
)

/** The weather backend: weatherapi.com in `prod`, canned data in `mock`. Main-safe; throws [WeatherDataException]. */
interface WeatherRemoteDataSource {
    suspend fun searchLocations(query: String): List<RemoteLocation>

    suspend fun currentWeather(locationId: Long): CityWeather
}
