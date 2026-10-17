package com.example.weathertracking.data.remote

import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.data.model.WeatherDataException
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/** Checks the weatherapi.com JSON mapping and error translation against a local fake server. */
class RetrofitWeatherRemoteDataSourceTest {

    private val server = MockWebServer()
    private lateinit var dataSource: RetrofitWeatherRemoteDataSource

    @Before
    fun setUp() {
        server.start()
        val api = WeatherApi.create(apiKey = "test-key", baseUrl = server.url("/v1/").toString())
        dataSource = RetrofitWeatherRemoteDataSource(api)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun searchLocations_mapsEveryLocation() = runTest {
        server.enqueue(json(SEARCH_RESPONSE))

        val locations = dataSource.searchLocations("Lon")

        assertEquals(
            listOf(
                RemoteLocation(2801268, "London", "City of London, Greater London", "United Kingdom"),
                RemoteLocation(315398, "London", "Ontario", "Canada"),
            ),
            locations,
        )
        val request = server.takeRequest()
        assertEquals("/v1/search.json", request.url.encodedPath)
        assertEquals("Lon", request.url.queryParameter("q"))
        assertEquals("test-key", request.url.queryParameter("key"))
    }

    @Test
    fun currentWeather_queriesByIdAndMapsTheReading() = runTest {
        server.enqueue(json(CURRENT_RESPONSE))

        val weather = dataSource.currentWeather(2801268)

        assertEquals(2801268L, weather.id)
        assertEquals("London", weather.name)
        assertEquals("United Kingdom", weather.country)
        assertEquals(14.3, weather.temperatureCelsius, 0.0)
        assertEquals(13.1, weather.feelsLikeCelsius, 0.0)
        assertEquals("Partly cloudy", weather.condition)
        assertEquals("https://cdn.weatherapi.com/weather/128x128/day/116.png", weather.iconUrl)
        assertEquals(72, weather.humidityPercent)
        assertEquals(3.0, weather.uvIndex, 0.0)
        assertEquals(1760709600L, weather.lastUpdatedEpochSeconds)
        assertEquals("id:2801268", server.takeRequest().url.queryParameter("q"))
    }

    @Test
    fun rejectedKey_isInvalidApiKey() = runTest {
        server.enqueue(MockResponse.Builder().code(401).body(ERROR_RESPONSE).build())

        assertFailure(FailureReason.InvalidApiKey) { dataSource.searchLocations("Lon") }
    }

    @Test
    fun serverError_isUnknown() = runTest {
        server.enqueue(MockResponse.Builder().code(500).build())

        assertFailure(FailureReason.Unknown) { dataSource.currentWeather(1) }
    }

    @Test
    fun malformedBody_isUnknown() = runTest {
        server.enqueue(json("""{"unexpected": true}"""))

        assertFailure(FailureReason.Unknown) { dataSource.currentWeather(1) }
    }

    @Test
    fun unreachableServer_isNetwork() = runTest {
        server.close()

        assertFailure(FailureReason.Network) { dataSource.searchLocations("Lon") }
    }

    private suspend fun assertFailure(expected: FailureReason, block: suspend () -> Unit) {
        try {
            block()
            fail("Expected WeatherDataException($expected)")
        } catch (e: WeatherDataException) {
            assertEquals(expected, e.reason)
        }
    }

    private fun json(body: String) = MockResponse.Builder()
        .code(200)
        .addHeader("Content-Type", "application/json")
        .body(body)
        .build()

    private companion object {
        val SEARCH_RESPONSE = """
            [
              {"id": 2801268, "name": "London", "region": "City of London, Greater London", "country": "United Kingdom",
               "lat": 51.52, "lon": -0.11, "url": "london-city-of-london-greater-london-united-kingdom"},
              {"id": 315398, "name": "London", "region": "Ontario", "country": "Canada",
               "lat": 42.98, "lon": -81.25, "url": "london-ontario-canada"}
            ]
        """.trimIndent()

        val CURRENT_RESPONSE = """
            {
              "location": {"name": "London", "region": "City of London, Greater London", "country": "United Kingdom",
                           "lat": 51.52, "lon": -0.11, "tz_id": "Europe/London", "localtime_epoch": 1760710000,
                           "localtime": "2025-10-17 15:06"},
              "current": {"last_updated_epoch": 1760709600, "last_updated": "2025-10-17 15:00", "temp_c": 14.3,
                          "temp_f": 57.7, "is_day": 1,
                          "condition": {"text": "Partly cloudy", "icon": "//cdn.weatherapi.com/weather/64x64/day/116.png", "code": 1003},
                          "wind_kph": 15.1, "humidity": 72, "cloud": 50, "feelslike_c": 13.1, "uv": 3.0}
            }
        """.trimIndent()

        const val ERROR_RESPONSE = """{"error": {"code": 2006, "message": "API key is invalid."}}"""
    }
}
