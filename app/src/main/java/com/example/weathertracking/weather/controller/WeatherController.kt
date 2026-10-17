package com.example.weathertracking.weather.controller

import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.weather.model.SearchState
import com.example.weathertracking.weather.model.WeatherModel
import javax.inject.Inject

/**
 * The **C** in MVC: turns raw user input from the View into operations on the [WeatherModel].
 *
 * Its job is *interpretation*, not storage:
 * - it cleans up what the user typed and decides whether it is worth a search,
 * - it maps gestures ("retry", "screen became visible") to the right Model operation.
 *
 * It holds no state and never pushes data to the View: the View learns about every change by
 * observing the Model. Because it is stateless it can simply be recreated with the Activity.
 */
class WeatherController @Inject constructor(
    private val model: WeatherModel,
) {

    /** The screen became visible: make sure the user is not looking at old weather. */
    fun onScreenStarted() {
        model.refreshSelectedCityIfStale()
    }

    /** The user submitted the search field. */
    fun onSearchSubmitted(rawQuery: String) {
        val query = rawQuery.trim().replace(WHITESPACE, " ")
        if (query.isEmpty()) {
            model.clearSearch()
            return
        }
        val current = model.search.value
        val alreadyShowing = (current is SearchState.Results && current.query.equals(query, ignoreCase = true)) ||
            (current is SearchState.Searching && current.query.equals(query, ignoreCase = true))
        if (!alreadyShowing) model.search(query)
    }

    /** The user cleared the search field or dismissed the results. */
    fun onSearchCleared() {
        model.clearSearch()
    }

    /** The user tapped "Retry" after a failed search. */
    fun onRetrySearchClicked() {
        val failed = model.search.value as? SearchState.Failed ?: return
        model.search(failed.query)
    }

    /** The user picked a city from the results. */
    fun onCityClicked(city: CityWeather) {
        model.selectCity(city)
    }

    /** The user tapped the refresh button. */
    fun onRefreshClicked() {
        model.refreshSelectedCity()
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
