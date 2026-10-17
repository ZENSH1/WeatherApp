package com.example.weathertracking.weather.model

import com.example.weathertracking.core.TimeSource
import com.example.weathertracking.core.di.ApplicationScope
import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.data.model.WeatherDataException
import com.example.weathertracking.data.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The **M** in MVC: the app's weather state plus the business rules that change it.
 *
 * - The **View** observes [selectedCity] and [search] and redraws whenever they change
 *   (classic MVC "change notification", implemented with [StateFlow]).
 * - The **Controller** decides *when* to call [search], [selectCity], [refreshSelectedCity]…
 * - The Model never references either of them, so it can be unit tested on the JVM.
 *
 * It is a singleton that runs its work in [ApplicationScope]: rotating the screen recreates the
 * View and the Controller, but this state (and any request in flight) survives. No ViewModel needed.
 */
@Singleton
class WeatherModel @Inject constructor(
    private val repository: WeatherRepository,
    private val timeSource: TimeSource,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.Idle)

    /** The city the user picked, combined with the status of its latest refresh. */
    val selectedCity: StateFlow<SelectedCityState> =
        combine(repository.selectedCity, refreshStatus) { city, refresh ->
            if (city == null) {
                SelectedCityState.None
            } else {
                SelectedCityState.Selected(
                    city = city,
                    isRefreshing = refresh == RefreshStatus.Running,
                    refreshFailure = (refresh as? RefreshStatus.Failed)?.reason,
                )
            }
        }.stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SelectedCityState.Loading)

    private val _search = MutableStateFlow<SearchState>(SearchState.Idle)

    /** The current search session. */
    val search: StateFlow<SearchState> = _search.asStateFlow()

    private var searchJob: Job? = null
    private var selectJob: Job? = null
    private var refreshJob: Job? = null

    /** Starts a search for [query]. Rule: only the latest search counts, an older one is abandoned. */
    fun search(query: String) {
        searchJob?.cancel()
        _search.value = SearchState.Searching(query)
        searchJob = scope.launch {
            _search.value = try {
                val cities = repository.search(query)
                if (cities.isEmpty()) SearchState.NoResults(query) else SearchState.Results(query, cities)
            } catch (e: WeatherDataException) {
                SearchState.Failed(query, e.reason)
            }
        }
    }

    /** Ends the search session. */
    fun clearSearch() {
        searchJob?.cancel()
        _search.value = SearchState.Idle
    }

    /**
     * Saves [city] and makes it the selected one.
     * Rules: picking a city ends the search session, and the latest pick wins.
     */
    fun selectCity(city: CityWeather) {
        clearSearch()
        refreshJob?.cancel()
        refreshStatus.value = RefreshStatus.Idle
        selectJob?.cancel()
        selectJob = scope.launch { repository.select(city) }
    }

    /** Downloads fresh weather for the selected city. Rule: one refresh at a time. */
    fun refreshSelectedCity() {
        if (refreshJob?.isActive == true) return
        refreshJob = scope.launch { refresh() }
    }

    /** Like [refreshSelectedCity], but only when the saved reading is older than [STALE_AFTER_SECONDS]. */
    fun refreshSelectedCityIfStale() {
        if (refreshJob?.isActive == true) return
        refreshJob = scope.launch {
            val city = repository.selectedCity.first() ?: return@launch
            val ageSeconds = timeSource.nowEpochSeconds() - city.lastUpdatedEpochSeconds
            if (ageSeconds >= STALE_AFTER_SECONDS) refresh()
        }
    }

    private suspend fun refresh() {
        refreshStatus.value = RefreshStatus.Running
        refreshStatus.value = try {
            repository.refreshSelectedCity()
            RefreshStatus.Idle
        } catch (e: WeatherDataException) {
            RefreshStatus.Failed(e.reason)
        }
    }

    private sealed interface RefreshStatus {
        data object Idle : RefreshStatus
        data object Running : RefreshStatus
        data class Failed(val reason: FailureReason) : RefreshStatus
    }

    companion object {
        /** Weather older than this is refreshed automatically when the screen starts. */
        const val STALE_AFTER_SECONDS = 30 * 60L

        /** Keep observing the database for a few seconds after the View stops (e.g. rotation). */
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
