package com.example.weathertracking.weather.controller

import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.weather.model.SearchState
import com.example.weathertracking.weather.model.WeatherModel
import javax.inject.Inject

/**
 * The **C** in MVC: interprets user input and calls the [WeatherModel].
 * Stateless, and never updates the View directly.
 */
class WeatherController @Inject constructor(
    private val model: WeatherModel,
) {

    fun onScreenStarted() {
        model.refreshSelectedCityIfStale()
    }

    fun onSearchSubmitted(rawQuery: String) {
        val query = rawQuery.trim().replace(WHITESPACE, " ")
        if (query.isEmpty()) {
            model.clearSearch()
            return
        }
        // Don't repeat the search that's already on screen.
        val current = model.search.value
        val alreadyShowing = (current is SearchState.Results && current.query.equals(query, ignoreCase = true)) ||
            (current is SearchState.Searching && current.query.equals(query, ignoreCase = true))
        if (!alreadyShowing) model.search(query)
    }

    fun onSearchCleared() {
        model.clearSearch()
    }

    fun onRetrySearchClicked() {
        val failed = model.search.value as? SearchState.Failed ?: return
        model.search(failed.query)
    }

    fun onCityClicked(city: CityWeather) {
        model.selectCity(city)
    }

    fun onRefreshClicked() {
        model.refreshSelectedCity()
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
