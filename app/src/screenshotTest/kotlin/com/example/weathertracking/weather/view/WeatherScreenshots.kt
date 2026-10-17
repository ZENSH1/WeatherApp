package com.example.weathertracking.weather.view

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.android.tools.screenshot.PreviewTest
import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.ui.theme.WeatherTrackingTheme
import com.example.weathertracking.weather.model.SearchState
import com.example.weathertracking.weather.model.SelectedCityState
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Screenshot tests. Each @PreviewTest is rendered on the JVM and compared with its reference image
// in src/screenshotTestMockDebug/reference (the README shows those images).
//   ./gradlew :app:updateMockDebugScreenshotTest     record new reference images
//   ./gradlew :app:validateMockDebugScreenshotTest   compare against them
// Every screenshot is a state of the View: the Model's state goes in, pixels come out.

@PreviewTest
@Preview(showSystemUi = true, device = Devices.PIXEL_7)
@Composable
fun NoCitySelectedScreenshot() {
    WeatherScreenshot(selectedCity = SelectedCityState.None)
}

@PreviewTest
@Preview(name = "light", showSystemUi = true, device = Devices.PIXEL_7)
@Preview(
    name = "dark",
    showSystemUi = true,
    device = Devices.PIXEL_7,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
fun SelectedCityScreenshot() {
    WeatherScreenshot(selectedCity = SelectedCityState.Selected(Lahore))
}

@PreviewTest
@Preview(showSystemUi = true, device = Devices.PIXEL_7)
@Composable
fun SearchResultsScreenshot() {
    WeatherScreenshot(
        selectedCity = SelectedCityState.Selected(Lahore),
        search = SearchState.Results(query = "Lon", cities = listOf(London, LongBeach, Londrina)),
        query = "Lon",
    )
}

@PreviewTest
@Preview(showSystemUi = true, device = Devices.PIXEL_7)
@Composable
fun SearchFailedScreenshot() {
    WeatherScreenshot(
        selectedCity = SelectedCityState.None,
        search = SearchState.Failed(query = "offline", reason = FailureReason.Network),
        query = "offline",
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun WeatherScreenshot(
    selectedCity: SelectedCityState,
    search: SearchState = SearchState.Idle,
    query: String = "",
) {
    // Reference images must not depend on the time zone or language of the machine rendering them.
    remember {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Locale.setDefault(Locale.US)
    }
    // Weather icons are downloaded at runtime; screenshots draw a stand-in sun instead.
    val previewHandler = remember { AsyncImagePreviewHandler { SunIcon } }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        WeatherTrackingTheme(dynamicColor = false) {
            WeatherView(
                selectedCity = selectedCity,
                search = search,
                onSearch = {},
                onClearSearch = {},
                onCityClick = {},
                onRetrySearch = {},
                onRefresh = {},
                initialQuery = query,
            )
        }
    }
}

private fun city(
    id: Long,
    name: String,
    region: String,
    country: String,
    temperatureCelsius: Double,
    condition: String = "Sunny",
) = CityWeather(
    id = id,
    name = name,
    region = region,
    country = country,
    temperatureCelsius = temperatureCelsius,
    feelsLikeCelsius = temperatureCelsius + 2,
    condition = condition,
    iconUrl = "https://example.com/icon.png",
    humidityPercent = 41,
    uvIndex = 7.0,
    // 2026-06-01 09:30 UTC
    lastUpdatedEpochSeconds = 1_780_306_200,
)

private val Lahore = city(1, "Lahore", "Punjab", "Pakistan", temperatureCelsius = 33.6)
private val London = city(2, "London", "City of London, Greater London", "United Kingdom", 18.2, "Clear")
private val LongBeach = city(3, "Long Beach", "California", "United States of America", 24.0)
private val Londrina = city(4, "Londrina", "Parana", "Brazil", 27.5)

/** A simple sun, drawn once. */
private val SunIcon: Image by lazy {
    val size = 256
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFB300.toInt()
        strokeWidth = 14f
        strokeCap = Paint.Cap.ROUND
    }
    val center = size / 2f
    canvas.drawCircle(center, center, 52f, paint)
    repeat(8) { ray ->
        val angle = ray * PI / 4
        val (dx, dy) = cos(angle).toFloat() to sin(angle).toFloat()
        canvas.drawLine(center + 76 * dx, center + 76 * dy, center + 108 * dx, center + 108 * dy, paint)
    }
    bitmap.asImage()
}
