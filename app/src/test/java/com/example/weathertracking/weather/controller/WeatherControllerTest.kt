package com.example.weathertracking.weather.controller

import com.example.weathertracking.core.TimeSource
import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.testing.FakeWeatherRepository
import com.example.weathertracking.testing.cityWeather
import com.example.weathertracking.weather.model.SearchState
import com.example.weathertracking.weather.model.WeatherModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Controller is tested against a real [WeatherModel] (it is cheap and has no Android code);
 * only the repository underneath is fake. Assertions look at what the Model ended up doing.
 * The Model runs in `backgroundScope`, so `runCurrent()` is used to let its work finish.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeatherControllerTest {

    private val repository = FakeWeatherRepository()
    private val now = 1_000_000L

    private fun TestScope.createController(): Pair<WeatherController, WeatherModel> {
        val model = WeatherModel(repository, TimeSource { now }, backgroundScope)
        return WeatherController(model) to model
    }

    @Test
    fun blankQuery_clearsTheSearchInsteadOfSearching() = runTest {
        val (controller, model) = createController()

        controller.onSearchSubmitted("   ")
        runCurrent()

        assertEquals(SearchState.Idle, model.search.value)
        assertTrue(repository.searchQueries.isEmpty())
    }

    @Test
    fun query_isTrimmedAndWhitespaceCollapsed() = runTest {
        val (controller, _) = createController()

        controller.onSearchSubmitted("  New    York ")
        runCurrent()

        assertEquals(listOf("New York"), repository.searchQueries)
    }

    @Test
    fun resubmittingTheQueryOnScreen_doesNotSearchAgain() = runTest {
        repository.searchResults = mapOf("Paris" to listOf(cityWeather(1, "Paris")))
        val (controller, _) = createController()

        controller.onSearchSubmitted("Paris")
        runCurrent()
        controller.onSearchSubmitted("paris")
        runCurrent()

        assertEquals(listOf("Paris"), repository.searchQueries)
    }

    @Test
    fun retry_rerunsTheFailedQuery() = runTest {
        repository.searchFailure = FailureReason.Network
        val (controller, model) = createController()
        controller.onSearchSubmitted("Cairo")
        runCurrent()

        repository.searchFailure = null
        controller.onRetrySearchClicked()
        runCurrent()

        assertEquals(listOf("Cairo", "Cairo"), repository.searchQueries)
        assertEquals(SearchState.NoResults("Cairo"), model.search.value)
    }

    @Test
    fun clickingACity_selectsIt() = runTest {
        val city = cityWeather(5)
        val (controller, _) = createController()

        controller.onCityClicked(city)
        runCurrent()

        assertEquals(city, repository.selected.value)
    }

    @Test
    fun screenStarted_refreshesStaleWeather() = runTest {
        repository.selected.value = cityWeather(1, lastUpdatedEpochSeconds = 0)
        val (controller, _) = createController()

        controller.onScreenStarted()
        runCurrent()

        assertEquals(1, repository.refreshCount)
    }
}
