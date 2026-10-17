package com.example.weathertracking.weather.model

import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.FailureReason

sealed interface SelectedCityState {

    data object Loading : SelectedCityState
    data object None : SelectedCityState

    data class Selected(
        val city: CityWeather,
        val isRefreshing: Boolean = false,
        /** The last refresh failed; [city] is the previous reading. */
        val refreshFailure: FailureReason? = null,
    ) : SelectedCityState
}

sealed interface SearchState {

    data object Idle : SearchState
    data class Searching(val query: String) : SearchState
    data class Results(val query: String, val cities: List<CityWeather>) : SearchState
    data class NoResults(val query: String) : SearchState
    data class Failed(val query: String, val reason: FailureReason) : SearchState
}
