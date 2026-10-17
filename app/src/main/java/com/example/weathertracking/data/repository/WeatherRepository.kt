package com.example.weathertracking.data.repository

import com.example.weathertracking.data.local.CityWeatherDao
import com.example.weathertracking.data.local.SelectedCityStore
import com.example.weathertracking.data.local.toEntity
import com.example.weathertracking.data.local.toModel
import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.WeatherDataException
import com.example.weathertracking.data.remote.WeatherRemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Single entry point to weather data for the Model. It hides where data comes from:
 * the network (search, refresh) or the local database and DataStore (the selected city).
 *
 * Every function is main-safe. Failures surface as [WeatherDataException].
 */
interface WeatherRepository {

    /** The city the user picked, read from the database and updated whenever it changes. */
    val selectedCity: Flow<CityWeather?>

    /** Finds cities matching [query] together with their current weather. */
    suspend fun search(query: String): List<CityWeather>

    /** Saves [city] locally and makes it the selected one. */
    suspend fun select(city: CityWeather)

    /** Downloads fresh weather for the selected city. Does nothing if no city is selected. */
    suspend fun refreshSelectedCity()
}

class DefaultWeatherRepository @Inject constructor(
    private val remote: WeatherRemoteDataSource,
    private val dao: CityWeatherDao,
    private val selection: SelectedCityStore,
) : WeatherRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val selectedCity: Flow<CityWeather?> = selection.selectedCityId
        .flatMapLatest { id -> if (id == null) flowOf(null) else dao.observe(id) }
        .map { entity -> entity?.toModel() }
        .distinctUntilChanged()

    override suspend fun search(query: String): List<CityWeather> {
        val locations = remote.searchLocations(query).take(MAX_RESULTS)
        // One request per location, in parallel. If any of them fails the whole search fails.
        return coroutineScope {
            locations.map { location -> async { remote.currentWeather(location.id) } }.awaitAll()
        }.distinctBy { it.id }
    }

    override suspend fun select(city: CityWeather) {
        dao.upsert(city.toEntity())
        selection.setSelectedCityId(city.id)
    }

    override suspend fun refreshSelectedCity() {
        val id = selection.selectedCityId.first() ?: return
        dao.upsert(remote.currentWeather(id).toEntity())
    }

    private companion object {
        const val MAX_RESULTS = 8
    }
}
