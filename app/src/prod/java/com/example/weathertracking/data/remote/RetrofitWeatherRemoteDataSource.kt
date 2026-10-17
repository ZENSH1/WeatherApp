package com.example.weathertracking.data.remote

import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.data.model.WeatherDataException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RetrofitWeatherRemoteDataSource @Inject constructor(
    private val api: WeatherApi,
) : WeatherRemoteDataSource {

    override suspend fun searchLocations(query: String): List<RemoteLocation> = call {
        api.search(query).map { RemoteLocation(it.id, it.name, it.region, it.country) }
    }

    override suspend fun currentWeather(locationId: Long): CityWeather = call {
        api.current("id:$locationId").toModel(locationId)
    }

    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (e: IOException) {
        throw WeatherDataException(FailureReason.Network, e)
    } catch (e: HttpException) {
        val reason = if (e.code() == 401 || e.code() == 403) FailureReason.InvalidApiKey else FailureReason.Unknown
        throw WeatherDataException(reason, e)
    } catch (e: SerializationException) {
        throw WeatherDataException(FailureReason.Unknown, e)
    }
}

private fun NetworkCurrentResponse.toModel(locationId: Long) = CityWeather(
    id = locationId,
    name = location.name,
    region = location.region,
    country = location.country,
    temperatureCelsius = current.temperatureCelsius,
    feelsLikeCelsius = current.feelsLikeCelsius,
    condition = current.condition.text,
    // Larger icon, absolute URL.
    iconUrl = current.condition.icon
        .replace("64x64", "128x128")
        .let { if (it.startsWith("//")) "https:$it" else it },
    humidityPercent = current.humidity,
    uvIndex = current.uv,
    lastUpdatedEpochSeconds = current.lastUpdatedEpoch,
)
