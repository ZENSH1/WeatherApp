package com.example.weathertracking.data.local

import kotlinx.coroutines.flow.Flow

/** Remembers which city the user picked, across app restarts. */
interface SelectedCityStore {
    /** The selected city's id, or `null` before the user has picked one. */
    val selectedCityId: Flow<Long?>

    suspend fun setSelectedCityId(id: Long)
}
