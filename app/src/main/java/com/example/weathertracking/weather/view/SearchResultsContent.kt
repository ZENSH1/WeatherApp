package com.example.weathertracking.weather.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.weathertracking.R
import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.weather.model.SearchState

/** Renders a [SearchState] other than [SearchState.Idle]. */
@Composable
internal fun SearchResultsContent(
    search: SearchState,
    onCityClick: (CityWeather) -> Unit,
    onRetry: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    when (search) {
        SearchState.Idle -> Unit

        is SearchState.Searching -> CenteredMessage(contentPadding, modifier) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.searching_for, search.query), textAlign = TextAlign.Center)
        }

        is SearchState.NoResults -> CenteredMessage(contentPadding, modifier) {
            Text(stringResource(R.string.no_results, search.query), textAlign = TextAlign.Center)
        }

        is SearchState.Failed -> CenteredMessage(contentPadding, modifier) {
            Text(
                text = stringResource(search.reason.messageRes()),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }

        is SearchState.Results -> LazyColumn(
            modifier = modifier,
            // Insets go into contentPadding (not a parent padding) so the list scrolls edge to edge.
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(search.cities, key = { it.id }, contentType = { "city" }) { city ->
                CityResultCard(city = city, onClick = { onCityClick(city) })
            }
        }
    }
}

@Composable
private fun CityResultCard(city: CityWeather, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = city.name, style = MaterialTheme.typography.titleMedium)
                if (city.subtitle.isNotEmpty()) {
                    Text(
                        text = city.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(text = city.condition, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.width(8.dp))
            TemperatureText(celsius = city.temperatureCelsius, style = MaterialTheme.typography.headlineMedium)
            AsyncImage(
                model = city.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
        }
    }
}

@Composable
private fun CenteredMessage(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}
