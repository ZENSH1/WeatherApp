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
 * The **M** in MVC: weather state and the rules that change it. The View observes it, the
 * Controller calls it, and it knows neither. App-scoped, so it survives rotation without a ViewModel.
 */
@Singleton
class WeatherModel @Inject constructor(
    private val repository: WeatherRepository,
    private val timeSource: TimeSource,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.Idle)

    /** The selected city plus the status of its latest refresh. */
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

    val search: StateFlow<SearchState> = _search.asStateFlow()

    private var searchJob: Job? = null
    private var selectJob: Job? = null
    private var refreshJob: Job? = null

    /** Only the latest search counts. */
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

    fun clearSearch() {
        searchJob?.cancel()
        _search.value = SearchState.Idle
    }

    /** Picking a city ends the search; the latest pick wins. */
    fun selectCity(city: CityWeather) {
        clearSearch()
        refreshJob?.cancel()
        refreshStatus.value = RefreshStatus.Idle
        selectJob?.cancel()
        selectJob = scope.launch { repository.select(city) }
    }

    /** One refresh at a time. */
    fun refreshSelectedCity() {
        if (refreshJob?.isActive == true) return
        refreshJob = scope.launch { refresh() }
    }

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
        const val STALE_AFTER_SECONDS = 30 * 60L

        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
