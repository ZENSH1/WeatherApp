package com.example.weathertracking.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** In-memory database on a real device. */
@RunWith(AndroidJUnit4::class)
class CityWeatherDaoTest {

    private lateinit var database: WeatherDatabase
    private lateinit var dao: CityWeatherDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDatabase::class.java,
        ).build()
        dao = database.cityWeatherDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observe_unknownId_emitsNull() = runTest {
        assertNull(dao.observe(42).first())
    }

    @Test
    fun upsert_thenObserve_returnsTheRow() = runTest {
        val row = entity(id = 1, temperature = 20.0)

        dao.upsert(row)

        assertEquals(row, dao.observe(1).first())
    }

    @Test
    fun upsert_existingId_replacesTheRow() = runTest {
        dao.upsert(entity(id = 1, temperature = 20.0))
        val updated = entity(id = 1, temperature = 25.0)

        dao.upsert(updated)

        assertEquals(updated, dao.observe(1).first())
    }

    private fun entity(id: Long, temperature: Double) = CityWeatherEntity(
        id = id,
        name = "City $id",
        region = "Region",
        country = "Country",
        temperatureCelsius = temperature,
        feelsLikeCelsius = temperature,
        condition = "Sunny",
        iconUrl = "",
        humidityPercent = 40,
        uvIndex = 3.0,
        lastUpdatedEpochSeconds = 0,
    )
}
