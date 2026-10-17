package com.example.weathertracking.testing

import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.data.model.WeatherDataException
import com.example.weathertracking.data.repository.WeatherRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** A scriptable [WeatherRepository] for Model and Controller tests. */
class FakeWeatherRepository : WeatherRepository {

    val selected = MutableStateFlow<CityWeather?>(null)
    override val selectedCity: Flow<CityWeather?> = selected

    /** Results returned by [search], keyed by query. */
    var searchResults: Map<String, List<CityWeather>> = emptyMap()

    /** When non-null, [search] fails with this reason. */
    var searchFailure: FailureReason? = null

    /** When non-null, [search] suspends until it is completed, so tests can observe in-flight state. */
    var searchGate: CompletableDeferred<Unit>? = null

    val searchQueries = mutableListOf<String>()

    /** What [refreshSelectedCity] stores as the new reading. Defaults to the current one. */
    var refreshedCity: CityWeather? = null
    var refreshFailure: FailureReason? = null
    var refreshGate: CompletableDeferred<Unit>? = null
    var refreshCount = 0
        private set

    override suspend fun search(query: String): List<CityWeather> {
        searchQueries += query
        searchGate?.await()
        searchFailure?.let { throw WeatherDataException(it) }
        return searchResults[query].orEmpty()
    }

    override suspend fun select(city: CityWeather) {
        selected.value = city
    }

    override suspend fun refreshSelectedCity() {
        refreshCount++
        refreshGate?.await()
        refreshFailure?.let { throw WeatherDataException(it) }
        val current = selected.value ?: return
        selected.value = refreshedCity ?: current
    }
}
