package com.example.weathertracking.weather.model

import app.cash.turbine.test
import com.example.weathertracking.core.TimeSource
import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.testing.FakeWeatherRepository
import com.example.weathertracking.testing.cityWeather
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The Model holds the business rules, so it gets the most tests. No Android, no UI:
 * just a fake repository and a test coroutine scope.
 *
 * The Model's "application scope" is the test's `backgroundScope`. Work launched there is run by
 * `runCurrent()` (`advanceUntilIdle()` intentionally ignores background work).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeatherModelTest {

    private val repository = FakeWeatherRepository()
    private val now = 1_000_000L

    private fun TestScope.createModel() = WeatherModel(
        repository = repository,
        timeSource = TimeSource { now },
        scope = backgroundScope,
    )

    /** `selectedCity` uses WhileSubscribed, so keep a subscriber alive to read its latest value. */
    private fun TestScope.observeSelectedCity(model: WeatherModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.selectedCity.collect {} }
    }

    @Test
    fun search_matchingCities_goesFromSearchingToResults() = runTest {
        val london = cityWeather(1, "London")
        repository.searchResults = mapOf("London" to listOf(london))
        val model = createModel()

        model.search("London")
        assertEquals(SearchState.Searching("London"), model.search.value)

        runCurrent()
        assertEquals(SearchState.Results("London", listOf(london)), model.search.value)
    }

    @Test
    fun search_noMatches_isNoResults() = runTest {
        val model = createModel()

        model.search("Atlantis")
        runCurrent()

        assertEquals(SearchState.NoResults("Atlantis"), model.search.value)
    }

    @Test
    fun search_failure_isFailedWithReason() = runTest {
        repository.searchFailure = FailureReason.Network
        val model = createModel()

        model.search("Paris")
        runCurrent()

        assertEquals(SearchState.Failed("Paris", FailureReason.Network), model.search.value)
    }

    @Test
    fun search_newSearch_abandonsThePreviousOne() = runTest {
        val first = cityWeather(1)
        val second = cityWeather(2)
        repository.searchResults = mapOf("first" to listOf(first), "second" to listOf(second))
        val gate = CompletableDeferred<Unit>()
        repository.searchGate = gate
        val model = createModel()

        model.search("first")
        runCurrent()
        model.search("second")
        runCurrent()
        gate.complete(Unit)
        runCurrent()

        assertEquals(SearchState.Results("second", listOf(second)), model.search.value)
        assertEquals(listOf("first", "second"), repository.searchQueries)
    }

    @Test
    fun clearSearch_returnsToIdle() = runTest {
        repository.searchGate = CompletableDeferred()
        val model = createModel()

        model.search("Tokyo")
        model.clearSearch()
        runCurrent()

        assertEquals(SearchState.Idle, model.search.value)
    }

    @Test
    fun selectedCity_followsTheRepository() = runTest {
        val model = createModel()

        model.selectedCity.test {
            assertEquals(SelectedCityState.Loading, awaitItem())
            assertEquals(SelectedCityState.None, awaitItem())

            val city = cityWeather(3)
            repository.selected.value = city
            assertEquals(SelectedCityState.Selected(city), awaitItem())
        }
    }

    @Test
    fun selectCity_savesTheCityAndEndsTheSearchSession() = runTest {
        val city = cityWeather(7)
        repository.searchResults = mapOf("Seven" to listOf(city))
        val model = createModel()
        observeSelectedCity(model)
        model.search("Seven")
        runCurrent()

        model.selectCity(city)
        assertEquals(SearchState.Idle, model.search.value)

        runCurrent()
        assertEquals(city, repository.selected.value)
        assertEquals(SelectedCityState.Selected(city), model.selectedCity.value)
    }

    @Test
    fun refresh_showsProgressThenTheNewReading() = runTest {
        val old = cityWeather(1, temperatureCelsius = 10.0)
        val fresh = old.copy(temperatureCelsius = 12.0)
        repository.selected.value = old
        repository.refreshedCity = fresh
        val gate = CompletableDeferred<Unit>()
        repository.refreshGate = gate
        val model = createModel()
        observeSelectedCity(model)

        model.refreshSelectedCity()
        runCurrent()
        assertEquals(SelectedCityState.Selected(old, isRefreshing = true), model.selectedCity.value)

        gate.complete(Unit)
        runCurrent()
        assertEquals(SelectedCityState.Selected(fresh), model.selectedCity.value)
    }

    @Test
    fun refresh_failure_keepsTheSavedReadingAndReportsWhy() = runTest {
        val city = cityWeather(1)
        repository.selected.value = city
        repository.refreshFailure = FailureReason.InvalidApiKey
        val model = createModel()
        observeSelectedCity(model)

        model.refreshSelectedCity()
        runCurrent()

        assertEquals(
            SelectedCityState.Selected(city, refreshFailure = FailureReason.InvalidApiKey),
            model.selectedCity.value,
        )
    }

    @Test
    fun refresh_whileAnotherIsRunning_isIgnored() = runTest {
        repository.selected.value = cityWeather(1)
        val gate = CompletableDeferred<Unit>()
        repository.refreshGate = gate
        val model = createModel()

        model.refreshSelectedCity()
        runCurrent()
        model.refreshSelectedCity()
        runCurrent()
        gate.complete(Unit)
        runCurrent()

        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun refreshIfStale_recentReading_isLeftAlone() = runTest {
        repository.selected.value = cityWeather(1, lastUpdatedEpochSeconds = now - 60)
        val model = createModel()

        model.refreshSelectedCityIfStale()
        runCurrent()

        assertEquals(0, repository.refreshCount)
    }

    @Test
    fun refreshIfStale_oldReading_isRefreshed() = runTest {
        repository.selected.value = cityWeather(1, lastUpdatedEpochSeconds = now - WeatherModel.STALE_AFTER_SECONDS)
        val model = createModel()

        model.refreshSelectedCityIfStale()
        runCurrent()

        assertEquals(1, repository.refreshCount)
    }
}
