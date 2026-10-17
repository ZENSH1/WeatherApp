package com.example.weathertracking.data.repository

import com.example.weathertracking.data.local.CityWeatherDao
import com.example.weathertracking.data.local.CityWeatherEntity
import com.example.weathertracking.data.local.SelectedCityStore
import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.data.model.WeatherDataException
import com.example.weathertracking.data.remote.RemoteLocation
import com.example.weathertracking.data.remote.WeatherRemoteDataSource
import com.example.weathertracking.testing.cityWeather
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class DefaultWeatherRepositoryTest {

    private val remote = FakeRemote()
    private val dao = FakeDao()
    private val selection = FakeSelectedCityStore()
    private val repository = DefaultWeatherRepository(remote, dao, selection)

    @Test
    fun search_returnsTheCurrentWeatherOfEveryMatch() = runTest {
        val karachi = cityWeather(1, "Karachi")
        val kabul = cityWeather(2, "Kabul")
        remote.locations = listOf(karachi.toLocation(), kabul.toLocation())
        remote.weather = listOf(karachi, kabul).associateBy { it.id }

        assertEquals(listOf(karachi, kabul), repository.search("Ka"))
    }

    @Test
    fun search_failure_surfacesTheReason() = runTest {
        remote.failure = FailureReason.Network

        try {
            repository.search("Ka")
            fail("Expected WeatherDataException")
        } catch (e: WeatherDataException) {
            assertEquals(FailureReason.Network, e.reason)
        }
    }

    @Test
    fun select_storesTheCityAndMakesItSelected() = runTest {
        val city = cityWeather(9)

        repository.select(city)

        assertEquals(9L, selection.id.value)
        assertEquals(city, repository.selectedCity.first())
    }

    @Test
    fun selectedCity_isNullBeforeAnythingIsPicked() = runTest {
        assertNull(repository.selectedCity.first())
    }

    @Test
    fun refresh_replacesTheStoredReading() = runTest {
        val old = cityWeather(4, temperatureCelsius = 1.0)
        val fresh = old.copy(temperatureCelsius = 3.0)
        repository.select(old)
        remote.weather = mapOf(old.id to fresh)

        repository.refreshSelectedCity()

        assertEquals(fresh, repository.selectedCity.first())
    }

    @Test
    fun refresh_withoutASelection_doesNothing() = runTest {
        repository.refreshSelectedCity()

        assertEquals(0, remote.weatherCalls)
    }

    private fun CityWeather.toLocation() = RemoteLocation(id, name, region, country)

    private class FakeRemote : WeatherRemoteDataSource {
        var locations: List<RemoteLocation> = emptyList()
        var weather: Map<Long, CityWeather> = emptyMap()
        var failure: FailureReason? = null
        var weatherCalls = 0

        override suspend fun searchLocations(query: String): List<RemoteLocation> {
            failure?.let { throw WeatherDataException(it) }
            return locations
        }

        override suspend fun currentWeather(locationId: Long): CityWeather {
            weatherCalls++
            failure?.let { throw WeatherDataException(it) }
            return weather.getValue(locationId)
        }
    }

    private class FakeDao : CityWeatherDao {
        private val rows = MutableStateFlow<Map<Long, CityWeatherEntity>>(emptyMap())

        override fun observe(id: Long): Flow<CityWeatherEntity?> = rows.map { it[id] }

        override suspend fun upsert(city: CityWeatherEntity) {
            rows.update { it + (city.id to city) }
        }
    }

    private class FakeSelectedCityStore : SelectedCityStore {
        val id = MutableStateFlow<Long?>(null)
        override val selectedCityId: Flow<Long?> = id

        override suspend fun setSelectedCityId(id: Long) {
            this.id.value = id
        }
    }
}
