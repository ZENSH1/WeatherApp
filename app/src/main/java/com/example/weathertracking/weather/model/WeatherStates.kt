package com.example.weathertracking.weather.model

import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.FailureReason

/** What the Model knows about the city the user picked. */
sealed interface SelectedCityState {

    /** Still reading the saved selection from disk. */
    data object Loading : SelectedCityState

    /** The user has never picked a city. */
    data object None : SelectedCityState

    data class Selected(
        val city: CityWeather,
        /** A refresh request is in flight. */
        val isRefreshing: Boolean = false,
        /** The last refresh failed. [city] still holds the previously saved reading. */
        val refreshFailure: FailureReason? = null,
    ) : SelectedCityState
}

/** The state of the current search session. */
sealed interface SearchState {

    /** No search on screen. */
    data object Idle : SearchState

    data class Searching(val query: String) : SearchState

    data class Results(val query: String, val cities: List<CityWeather>) : SearchState

    data class NoResults(val query: String) : SearchState

    data class Failed(val query: String, val reason: FailureReason) : SearchState
}
