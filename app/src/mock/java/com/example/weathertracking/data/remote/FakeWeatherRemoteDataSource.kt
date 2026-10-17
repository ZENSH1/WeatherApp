package com.example.weathertracking.data.remote

import com.example.weathertracking.core.TimeSource
import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.data.model.WeatherDataException
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/** Canned cities with simulated latency. Readings drift every 10 minutes; searching "offline" fails. */
@Singleton
class FakeWeatherRemoteDataSource @Inject constructor(
    private val timeSource: TimeSource,
) : WeatherRemoteDataSource {

    override suspend fun searchLocations(query: String): List<RemoteLocation> {
        delay(SEARCH_LATENCY_MILLIS)
        if (query.contains(OFFLINE_QUERY, ignoreCase = true)) {
            throw WeatherDataException(FailureReason.Network)
        }
        return CITIES
            .filter { city ->
                city.name.contains(query, ignoreCase = true) ||
                    city.region.contains(query, ignoreCase = true) ||
                    city.country.contains(query, ignoreCase = true)
            }
            .map { RemoteLocation(it.id, it.name, it.region, it.country) }
    }

    override suspend fun currentWeather(locationId: Long): CityWeather {
        delay(WEATHER_LATENCY_MILLIS)
        val city = CITIES.firstOrNull { it.id == locationId }
            ?: throw WeatherDataException(FailureReason.Unknown)
        val now = timeSource.nowEpochSeconds()
        val drift = ((now / 600 + city.id) % 5 - 2).toInt()
        return CityWeather(
            id = city.id,
            name = city.name,
            region = city.region,
            country = city.country,
            temperatureCelsius = city.temperatureCelsius + drift,
            feelsLikeCelsius = city.temperatureCelsius + drift + city.feelsLikeOffset,
            condition = city.condition,
            iconUrl = "https://cdn.weatherapi.com/weather/128x128/day/${city.iconCode}.png",
            humidityPercent = (city.humidityPercent + drift * 3).coerceIn(0, 100),
            uvIndex = city.uvIndex,
            lastUpdatedEpochSeconds = now - now % 900,
        )
    }

    private data class FakeCity(
        val id: Long,
        val name: String,
        val region: String,
        val country: String,
        val temperatureCelsius: Double,
        val feelsLikeOffset: Double,
        val condition: String,
        val iconCode: Int,
        val humidityPercent: Int,
        val uvIndex: Double,
    )

    private companion object {
        const val SEARCH_LATENCY_MILLIS = 400L
        const val WEATHER_LATENCY_MILLIS = 250L
        const val OFFLINE_QUERY = "offline"

        val CITIES = listOf(
            FakeCity(1001, "Karachi", "Sindh", "Pakistan", 31.0, 3.0, "Sunny", 113, 58, 8.0),
            FakeCity(1002, "Lahore", "Punjab", "Pakistan", 29.0, 2.0, "Mist", 143, 64, 6.0),
            FakeCity(1003, "Islamabad", "Islamabad Capital Territory", "Pakistan", 24.0, 0.0, "Clear", 113, 45, 7.0),
            FakeCity(1004, "London", "City of London, Greater London", "United Kingdom", 14.0, -1.0, "Partly cloudy", 116, 72, 3.0),
            FakeCity(1005, "London", "Ontario", "Canada", 8.0, -3.0, "Cloudy", 119, 81, 2.0),
            FakeCity(1006, "Londonderry", "Northern Ireland", "United Kingdom", 11.0, -2.0, "Light drizzle", 266, 88, 1.0),
            FakeCity(1007, "New York", "New York", "United States of America", 18.0, 0.0, "Overcast", 122, 66, 4.0),
            FakeCity(1008, "Paris", "Ile-de-France", "France", 16.0, -1.0, "Light rain", 296, 79, 3.0),
            FakeCity(1009, "Tokyo", "Tokyo", "Japan", 21.0, 1.0, "Cloudy", 119, 70, 5.0),
            FakeCity(1010, "Sydney", "New South Wales", "Australia", 19.0, 0.0, "Sunny", 113, 55, 9.0),
            FakeCity(1011, "Dubai", "Dubai", "United Arab Emirates", 34.0, 4.0, "Sunny", 113, 40, 10.0),
            FakeCity(1012, "Toronto", "Ontario", "Canada", 9.0, -2.0, "Moderate rain", 302, 85, 2.0),
            FakeCity(1013, "Cairo", "Al Qahirah", "Egypt", 28.0, 1.0, "Sunny", 113, 35, 9.0),
            FakeCity(1014, "Berlin", "Berlin", "Germany", 12.0, -1.0, "Overcast", 122, 74, 2.0),
            FakeCity(1015, "Istanbul", "Istanbul", "Turkey", 19.0, 0.0, "Partly cloudy", 116, 68, 5.0),
            FakeCity(1016, "Mumbai", "Maharashtra", "India", 30.0, 4.0, "Patchy rain nearby", 176, 80, 7.0),
            FakeCity(1017, "Reykjavik", "Capital Region", "Iceland", 3.0, -4.0, "Light snow", 326, 83, 1.0),
            FakeCity(1018, "Nairobi", "Nairobi Area", "Kenya", 22.0, 0.0, "Thundery outbreaks nearby", 200, 62, 8.0),
            FakeCity(1019, "Rio de Janeiro", "Rio de Janeiro", "Brazil", 27.0, 2.0, "Partly cloudy", 116, 73, 9.0),
            FakeCity(1020, "Mexico City", "Distrito Federal", "Mexico", 20.0, 0.0, "Patchy rain nearby", 176, 60, 7.0),
        )
    }
}
