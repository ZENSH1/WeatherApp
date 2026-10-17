package com.example.weathertracking.weather.view

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.weathertracking.R
import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.weather.model.SelectedCityState
import java.util.Date
import kotlin.math.roundToInt

/** Renders [SelectedCityState]: a spinner, an empty state, or the selected city's weather. */
@Composable
internal fun SelectedCityContent(
    state: SelectedCityState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        SelectedCityState.Loading -> Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        SelectedCityState.None -> EmptyState(modifier)
        is SelectedCityState.Selected -> CurrentWeather(state, onRefresh, modifier)
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_city_selected),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.please_search),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CurrentWeather(
    state: SelectedCityState.Selected,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val city = state.city
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = city.iconUrl,
            contentDescription = city.condition,
            modifier = Modifier.size(160.dp),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(4.dp))
            Text(text = city.name, style = MaterialTheme.typography.headlineMedium)
        }
        if (city.subtitle.isNotEmpty()) {
            Text(
                text = city.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        TemperatureText(celsius = city.temperatureCelsius, style = MaterialTheme.typography.displayLarge)
        Text(text = city.condition, style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(24.dp))
        WeatherDetails(city)
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.updated_at, formatTime(city.lastUpdatedEpochSeconds)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                if (state.isRefreshing) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                }
            }
        }
        state.refreshFailure?.let { reason ->
            Text(
                text = stringResource(R.string.refresh_failed) + "\n" + stringResource(reason.messageRes()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WeatherDetails(city: CityWeather, modifier: Modifier = Modifier) {
    ElevatedCard(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            WeatherStat(
                label = stringResource(R.string.humidity),
                value = stringResource(R.string.humidity_value, city.humidityPercent),
            )
            WeatherStat(
                label = stringResource(R.string.uv_index),
                value = stringResource(R.string.uv_value, city.uvIndex),
            )
            WeatherStat(
                label = stringResource(R.string.feels_like),
                value = stringResource(R.string.temperature_value, city.feelsLikeCelsius.roundToInt()),
            )
        }
    }
}

@Composable
private fun WeatherStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge)
    }
}

/** Formats a timestamp with the user's 12/24-hour preference, e.g. "14:30" or "2:30 PM". */
@Composable
private fun formatTime(epochSeconds: Long): String {
    val context = LocalContext.current
    return remember(epochSeconds, context) {
        DateFormat.getTimeFormat(context).format(Date(epochSeconds * 1_000))
    }
}
