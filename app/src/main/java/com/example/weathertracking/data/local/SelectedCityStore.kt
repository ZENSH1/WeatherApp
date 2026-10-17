package com.example.weathertracking.data.local

import kotlinx.coroutines.flow.Flow

/** Remembers the picked city across restarts. */
interface SelectedCityStore {
    val selectedCityId: Flow<Long?>

    suspend fun setSelectedCityId(id: Long)
}
