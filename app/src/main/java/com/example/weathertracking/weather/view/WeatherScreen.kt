package com.example.weathertracking.weather.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.ui.theme.WeatherTrackingTheme
import com.example.weathertracking.weather.controller.WeatherController
import com.example.weathertracking.weather.model.SearchState
import com.example.weathertracking.weather.model.SelectedCityState
import com.example.weathertracking.weather.model.WeatherModel

/** The **V** in MVC: observes the Model and hands every gesture to the Controller. No logic. */
@Composable
fun WeatherScreen(
    model: WeatherModel,
    controller: WeatherController,
    modifier: Modifier = Modifier,
) {
    val selectedCity by model.selectedCity.collectAsStateWithLifecycle()
    val search by model.search.collectAsStateWithLifecycle()

    // Becoming visible is input too.
    LifecycleStartEffect(controller) {
        controller.onScreenStarted()
        onStopOrDispose { }
    }

    WeatherView(
        selectedCity = selectedCity,
        search = search,
        onSearch = controller::onSearchSubmitted,
        onClearSearch = controller::onSearchCleared,
        onCityClick = controller::onCityClicked,
        onRetrySearch = controller::onRetrySearchClicked,
        onRefresh = controller::onRefreshClicked,
        modifier = modifier,
    )
}

/** Stateless View: draws what it is given and reports gestures. [initialQuery] is for previews. */
@Composable
fun WeatherView(
    selectedCity: SelectedCityState,
    search: SearchState,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCityClick: (CityWeather) -> Unit,
    onRetrySearch: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    initialQuery: String = "",
) {
    // Typed text is View state; the Controller only sees it on submit.
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = modifier,
        // Includes the keyboard.
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                SearchField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = {
                        keyboardController?.hide()
                        onSearch(query)
                    },
                    onClear = {
                        query = ""
                        onClearSearch()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        },
    ) { innerPadding ->
        if (search == SearchState.Idle) {
            SelectedCityContent(
                state = selectedCity,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            )
        } else {
            SearchResultsContent(
                search = search,
                onCityClick = { city ->
                    query = ""
                    keyboardController?.hide()
                    onCityClick(city)
                },
                onRetry = onRetrySearch,
                contentPadding = innerPadding.plusAll(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding),
            )
        }
    }
}

@Composable
private fun PaddingValues.plusAll(extra: Dp): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + extra,
        top = calculateTopPadding() + extra,
        end = calculateEndPadding(layoutDirection) + extra,
        bottom = calculateBottomPadding() + extra,
    )
}

private val PreviewCity = CityWeather(
    id = 1,
    name = "Karachi",
    region = "Sindh",
    country = "Pakistan",
    temperatureCelsius = 31.4,
    feelsLikeCelsius = 34.0,
    condition = "Sunny",
    iconUrl = "",
    humidityPercent = 58,
    uvIndex = 8.0,
    lastUpdatedEpochSeconds = 0,
)

@PreviewLightDark
@Composable
private fun SelectedCityPreview() {
    WeatherTrackingTheme(dynamicColor = false) {
        WeatherView(
            selectedCity = SelectedCityState.Selected(PreviewCity),
            search = SearchState.Idle,
            onSearch = {},
            onClearSearch = {},
            onCityClick = {},
            onRetrySearch = {},
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    WeatherTrackingTheme(dynamicColor = false) {
        WeatherView(
            selectedCity = SelectedCityState.None,
            search = SearchState.Idle,
            onSearch = {},
            onClearSearch = {},
            onCityClick = {},
            onRetrySearch = {},
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultsPreview() {
    WeatherTrackingTheme(dynamicColor = false) {
        WeatherView(
            selectedCity = SelectedCityState.None,
            search = SearchState.Results(
                query = "Ka",
                cities = listOf(PreviewCity, PreviewCity.copy(id = 2, name = "Kabul", region = "Kabul", country = "Afghanistan")),
            ),
            onSearch = {},
            onClearSearch = {},
            onCityClick = {},
            onRetrySearch = {},
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FailedPreview() {
    WeatherTrackingTheme(dynamicColor = false) {
        WeatherView(
            selectedCity = SelectedCityState.None,
            search = SearchState.Failed(query = "offline", reason = FailureReason.Network),
            onSearch = {},
            onClearSearch = {},
            onCityClick = {},
            onRetrySearch = {},
            onRefresh = {},
        )
    }
}
