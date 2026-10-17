package com.example.weathertracking.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DataStoreSelectedCityStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SelectedCityStore {

    override val selectedCityId: Flow<Long?> = dataStore.data
        // Treat a read error as "nothing selected".
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences -> preferences[SELECTED_CITY_ID] }
        .distinctUntilChanged()

    override suspend fun setSelectedCityId(id: Long) {
        dataStore.edit { preferences -> preferences[SELECTED_CITY_ID] = id }
    }

    private companion object {
        val SELECTED_CITY_ID = longPreferencesKey("selected_city_id")
    }
}
